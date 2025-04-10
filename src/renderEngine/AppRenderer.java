package renderEngine;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import main.Image;
import main.apps.App;
import main.apps.MainApp;
import renderEngine.fonts.TextFont;
import sutil.SUtil;
import sutil.math.SVector;
import sutil.ui.UIContainer;
import sutil.ui.UIElement;
import sutil.ui.UIFloatContainer;
import sutil.ui.UIScale;
import sutil.ui.UIText;
import sutil.ui.UIToggle;
import ui.AppUI;
import ui.Colors;
import ui.Sizes;
import ui.components.AlphaScale;
import ui.components.HueSatField;
import ui.components.LightnessScale;
import ui.components.UIColorElement;

public class AppRenderer<T extends App> {

    private static final double BACKGROUND_DEPTH = 0.0, FOREGROUND_DEPTH = -0.5;

    protected T app;

    protected UIRenderMaster uiMaster;

    // true while float elements are being drawn
    protected boolean foregroundDraw;

    public AppRenderer(T app) {
        this.app = app;
        uiMaster = new UIRenderMaster(app);
    }

    public void render() {
        setDefaultBGColor();

        uiMaster.start();
        renderUI();
        uiMaster.stop();
    }

    protected void setDefaultBGColor() {
        uiMaster.setBGColor(Colors.getBackgroundNormalColor());
    }

    protected void renderUI() {
        uiMaster.resetMatrix();
        AppUI<?> ui = app.getUI();

        // uiMaster.textFont(ui.getFont());
        // uiMaster.textSize(ui.getTextSize());

        foregroundDraw = false;

        renderUIElement(ui.getRoot());
    }

    private void renderUIElement(UIElement element) {
        SVector position = element.getPosition();
        SVector size = element.getSize();

        SVector bgColor = element.getBackgroundColor();
        SVector olColor = element.getOutlineColor();

        boolean oldForegroundDraw = foregroundDraw;

        if (element instanceof UIFloatContainer) {
            foregroundDraw = true;
            // uiMaster.depth(-0.5);
            uiMaster.pushScissor();
            uiMaster.noScissor();
        }
        uiMaster.depth(foregroundDraw ? FOREGROUND_DEPTH : BACKGROUND_DEPTH);

        if (element instanceof UIColorElement e && bgColor != null) {
            uiMaster.checkerboardFill(Colors.getTransparentColors(), 15);
            uiMaster.noStroke();
            uiMaster.rect(position, size);

            uiMaster.fillAlpha(SUtil.alpha(e.getColor()) / 255.0);
        }

        if (bgColor != null) {
            uiMaster.fill(bgColor);
            uiMaster.noStroke();
            uiMaster.rect(position, size);
        }

        uiMaster.fillAlpha(1.0);

        if (element instanceof HueSatField) {
            uiMaster.hueSatField(position, size, App.isCircularHueSatField(), App.isHSLColorSpace());
        }
        if (element instanceof UIToggle toggle) {
            uiMaster.fill(Colors.getBackgroundHighlightColor2());
            double wh = size.y;
            double difference = size.x - size.y;
            uiMaster.ellipse(new SVector(position.x, position.y), new SVector(wh, wh));
            uiMaster.ellipse(new SVector(position.x + size.x - wh, position.y), new SVector(wh, wh));
            uiMaster.noStroke();
            uiMaster.rect(new SVector(position.x + wh / 2, position.y), new SVector(difference, wh));

            uiMaster.fill(Colors.getTextColor());
            double x = position.x + (toggle.getState() ? difference : 0);
            SVector pos = new SVector(x, position.y);
            SVector s = new SVector(wh, wh);
            final double factor = 0.7;
            pos.add(s.copy().scale((1 - factor) / 2));
            s.scale(factor);
            uiMaster.ellipse(pos, s);
        }
        if (element instanceof UIText text) {
            String fontName = text.getFontName();
            double textSize = text.getTextSize();
            // int textSize = (int) app.getUI().getTextSize();
            TextFont font = app.getLoader().loadFont(fontName, (int) textSize, false);
            uiMaster.textFont(font);
            uiMaster.textSize(textSize);
            uiMaster.fill(text.getColor());
            uiMaster.text(text.getText(), position);
        }
        if (element instanceof UIContainer container) {
            boolean isScrollable = container.isHScroll() || container.isVScroll();
            if (isScrollable) {
                uiMaster.pushScissor();
                uiMaster.scissor(position, size);
            }

            uiMaster.pushMatrix();
            uiMaster.translate(position);

            for (UIElement child : container.getChildren()) {
                renderUIElement(child);
            }

            uiMaster.popMatrix();
            if (isScrollable) {
                uiMaster.popScissor();
            }
        }
        if (element instanceof UIScale scale) {
            SVector pos = new SVector(position).add(scale.getScaleOffset());
            SVector siz = scale.getScaleSize();
            if (scale instanceof LightnessScale l) {
                uiMaster.lightnessScale(pos, siz, l.getHue(), l.getSaturation(), l.getOrientation(),
                        App.isHSLColorSpace());
            }
            if (scale instanceof AlphaScale a) {
                // checkerboard background
                uiMaster.noStroke();
                uiMaster.checkerboardFill(Colors.getTransparentColors(), siz.y / 2);
                uiMaster.rect(pos, siz);

                // color gradient
                uiMaster.fill(MainApp.toSVector(a.getRGB()));
                uiMaster.alphaScale(pos, siz, a.getOrientation());
            }
        }

        boolean doOutline = false;
        if (App.showDebugOutline()) {
            uiMaster.stroke(new SVector(1, 0.7, 0.1));
            uiMaster.strokeWeight(Sizes.STROKE_WEIGHT.size);
            doOutline = true;
        } else if (olColor != null) {
            uiMaster.stroke(olColor);
            uiMaster.strokeWeight(element.getStrokeWeight());
            doOutline = true;
        }
        if (doOutline) {
            uiMaster.noFill();
            uiMaster.rect(position, size);
        }

        if (element instanceof UIFloatContainer) {
            uiMaster.popScissor();
        }

        foregroundDraw = oldForegroundDraw;
    }

    public void reloadShaders() {
        uiMaster = new UIRenderMaster(app);
    }

    // https://computergraphics.stackexchange.com/questions/4936/lwjgl-opengl-get-bufferedimage-from-texture-id
    public void renderTextToImage(String text, int x, int y, int size, SVector color, TextFont font, Image image) {
        uiMaster.start();
        uiMaster.textFramebuffer();

        uiMaster.setBGColor(color, 0.0);
        uiMaster.fill(color);
        uiMaster.textFont(font);
        uiMaster.textSize(size);
        uiMaster.text(text, new SVector());

        uiMaster.stop();

        FrameBufferObject fbo = uiMaster.getTextFBO();
        int width = fbo.width(), height = fbo.height();

        // int format = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0,
        // GL11.GL_TEXTURE_INTERNAL_FORMAT);
        // System.out.println("format = " + format);
        // int channels = 4;
        // if (format == GL11.GL_RGB)
        // channels = 3;
        int format = GL11.GL_RGBA;

        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, fbo.textureID());
        GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, format, GL11.GL_UNSIGNED_BYTE, buffer);

        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        int i = 0;
        for (int yoff = 0; yoff < height; yoff++) {
            for (int xoff = 0; xoff < width; xoff++) {
                int r = buffer.get(i++) & 0xFF,
                        g = buffer.get(i++) & 0xFF,
                        b = buffer.get(i++) & 0xFF,
                        a = buffer.get(i++) & 0xFF;
                bufferedImage.setRGB(xoff, yoff, SUtil.toARGB(r, g, b, a));
            }
        }

        image.setSubImage(bufferedImage, x, y);
    }
}