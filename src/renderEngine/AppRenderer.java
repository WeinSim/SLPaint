package renderEngine;

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
import ui.components.TextFloatContainer;
import ui.components.UIColorElement;

public class AppRenderer<T extends App> {

    private static final double MIN_DEPTH = -1.0, MAX_DEPTH = 1.0;
    private static final int MIN_LAYER = 0, MAX_LAYER = 10, START_LAYER = 5;

    protected T app;

    protected UIRenderMaster uiMaster;

    protected int layer;

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

        layer = START_LAYER;

        renderUIElement(ui.getRoot());
    }

    private void renderUIElement(UIElement element) {
        SVector position = element.getPosition();
        SVector size = element.getSize();

        SVector bgColor = element.getBackgroundColor();
        SVector olColor = element.getOutlineColor();

        int oldLayer = layer;

        if (element instanceof UIFloatContainer) {
            // Not beautiful but it works for now.
            // Without this check, the text inside of a TextFloatContainer would render
            // above the rest of the UI.
            if (element instanceof TextFloatContainer) {
                layer = START_LAYER - 1;
            } else {
                layer++;
            }
            uiMaster.pushScissor();
            uiMaster.noScissor();
        }
        uiMaster.depth(SUtil.map(layer, MIN_LAYER, MAX_LAYER, MAX_DEPTH, MIN_DEPTH));

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
            TextFont font = app.getLoader().loadFont(fontName);
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

        layer = oldLayer;
    }

    public void reloadShaders() {
        uiMaster = new UIRenderMaster(app);
    }

    public void renderTextToImage(String text, int x, int y, int size, SVector color, TextFont font, Image image) {
        uiMaster.start();
        uiMaster.textFramebuffer();

        uiMaster.setBGColor(color, 0.0);
        // uiMaster.setBGColor(new SVector(), 0.0);
        uiMaster.fill(color);
        uiMaster.textFont(font);
        uiMaster.textSize(size);
        uiMaster.text(text, new SVector());

        uiMaster.stop();

        FrameBufferObject fbo = uiMaster.getTextFBO();
        int width = fbo.width(), height = fbo.height();

        int[] array = new int[width * height * 4];
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, fbo.textureID());
        GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, GL11.GL_INT, array);

        // final int divisor = 1 << (31 - 8);
        final int divisor = (int) (Integer.MAX_VALUE / 255.0);
        for (int i = 0; i < array.length; i++) {
            array[i] /= divisor;
        }
        image.drawSubImage(x, y, width, height, array);
    }
}