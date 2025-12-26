package renderEngine;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import main.Image;
import main.apps.App;
import main.apps.MainApp;
import renderEngine.fonts.TextFont;
import renderEngine.shaders.bufferobjects.FrameBufferObject;
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

    /**
     * Depth value usage: 1.0 - 0.0 is used for the base UI,
     * 0.0 - -1.0 is used for floating UI elements.
     * 
     * Each of the two regions (positive / negative depth value) is divided into
     * NUM_DEPTH_SUBDIVISIONS divions. Every layer gets
     * NUM_BACKGROUND_LAYER_SUBDIVISIONS starting from the backmost layer and
     * NUM_FOREGROUND_LAYER_SUBDIVIONS starting from the foremost layer.
     */
    private static final int NUM_DEPTH_SUBDIVISIONS = 1024;
    private static final int NUM_BACKGROUND_LAYER_SUBDIVISIONS = 3;
    private static final int NUM_FOREGROUND_LAYER_SUBVIDISIONS = 1;
    private static final int NUM_LAYERS = NUM_DEPTH_SUBDIVISIONS
            / (2 * (NUM_BACKGROUND_LAYER_SUBDIVISIONS + NUM_FOREGROUND_LAYER_SUBVIDISIONS));

    protected T app;

    protected UIRenderMaster uiMaster;

    /**
     * When this is set to true, the depth value will be negative (foreground UI,
     * see above). When it is set to false, the depth value will be positive (base
     * UI).
     */
    protected boolean foregroundDraw;
    protected int layer;

    public AppRenderer(T app) {
        this.app = app;
        uiMaster = new UIRenderMaster(app, app.getLoader());
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

        layer = NUM_LAYERS / 2;
        foregroundDraw = false;

        renderUIElement(ui.getRoot());
    }

    private void renderUIElement(UIElement element) {
        // System.out.format("Rendering UIElement \"%s\", position = %s, size = %s\n",
        // element.getClass().getName(), element.getPosition().toString(),
        // element.getSize().toString());
        int oldLayer = layer;
        boolean oldForegroundDraw = foregroundDraw;

        SVector position = element.getPosition();
        SVector size = element.getSize();

        if (element instanceof UIFloatContainer) {
            // Not beautiful but it works for now.
            // Without this check, the text inside of a TextFloatContainer would render
            // above the rest of the UI.
            if (element instanceof TextFloatContainer) {
                foregroundDraw = false;
                layer = 1;
            } else {
                foregroundDraw = true;
            }
            uiMaster.pushClipArea();
            uiMaster.noClipArea();
        }

        // background
        SVector bgColor = element.getBackgroundColor();
        if (element instanceof UIColorElement e) {
            if (bgColor != null) {
                // this is just a hack for now because we cannot guarantee the "color"-rect to
                // render above the checkered background rect
                uiMaster.depth(getDepth(0, false));

                double alpha = SUtil.alpha(e.getColor()) / 255.0;
                SVector[] checkerboardColors = Colors.getTransparentColors();
                SVector c0 = new SVector(checkerboardColors[0]).lerp(bgColor, alpha),
                        c1 = new SVector(checkerboardColors[1]).lerp(bgColor, alpha);

                uiMaster.checkerboardFill(new SVector[] { c0, c1 }, 15);
                uiMaster.noStroke();
                uiMaster.rect(position, size);

                bgColor = null;
            }
        }

        uiMaster.depth(getDepth(1, false));
        if (bgColor != null) {
            uiMaster.fill(bgColor);
            uiMaster.noStroke();
            uiMaster.rect(position, size);
        }
        uiMaster.fillAlpha(1.0);

        // outline
        SVector olColor = element.getOutlineColor();
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
            // why is the outline on division 0?
            uiMaster.depth(getDepth(0, true));
            uiMaster.noFill();
            uiMaster.rect(position, size);
        }

        // content
        uiMaster.depth(getDepth(2, false));
        if (element instanceof UIContainer container) {
            boolean isScrollable = container.isHScroll() || container.isVScroll();
            if (isScrollable) {
                uiMaster.pushClipArea();
                uiMaster.clipArea(position, size);
            }
            uiMaster.pushMatrix();
            uiMaster.translate(position);

            layer++;
            for (UIElement child : container.getChildren()) {
                renderUIElement(child);
            }
            layer--;
            uiMaster.depth(getDepth(2, false));

            uiMaster.popMatrix();
            if (isScrollable) {
                uiMaster.popClipArea();
            }
        }
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
        if (element instanceof UIScale scale) {
            SVector pos = new SVector(position).add(scale.getScaleOffset());
            SVector siz = scale.getScaleSize();
            if (scale instanceof LightnessScale l) {
                uiMaster.lightnessScale(pos, siz, l.getHue(), l.getSaturation(),
                        l.getOrientation() == UIContainer.VERTICAL, App.isHSLColorSpace());
            }
            if (scale instanceof AlphaScale a) {
                // checkerboard background
                uiMaster.noStroke();
                uiMaster.checkerboardFill(Colors.getTransparentColors(), siz.y / 2);
                uiMaster.rect(pos, siz);

                // color gradient
                uiMaster.fill(MainApp.toSVector(a.getRGB()));
                uiMaster.alphaScale(pos, siz, a.getOrientation() == UIContainer.VERTICAL);
            }
        }

        if (element instanceof UIFloatContainer) {
            uiMaster.popClipArea();
        }

        layer = oldLayer;
        foregroundDraw = oldForegroundDraw;
    }

    protected double getDepth(int division, boolean foreground) {
        int actualLayer = foreground
                ? NUM_FOREGROUND_LAYER_SUBVIDISIONS * layer + division
                : NUM_DEPTH_SUBDIVISIONS - 1 - NUM_BACKGROUND_LAYER_SUBDIVISIONS * layer - division;
        double depth = actualLayer * 1.0 / NUM_DEPTH_SUBDIVISIONS + (foregroundDraw ? -1 : 0);
        // System.out.format("getDepth(%d, %b) = %.3f\n", layer, foregroundDraw, depth);
        return depth;
    }

    public void reloadShaders() {
        uiMaster = new UIRenderMaster(app, app.getLoader());
    }

    public void renderTextToImage(String text, int x, int y, int size, SVector color, TextFont font, Image image) {
        uiMaster.start();
        uiMaster.textFramebuffer();

        // For this text rendering, we only care about the alpha output.
        // The color channel should be filled with the text color.
        GL30.glBlendFuncSeparate(
                GL11.GL_ONE, GL11.GL_ONE, // rgb
                GL11.GL_ONE_MINUS_DST_ALPHA, GL11.GL_ONE); // alpha

        uiMaster.setBGColor(new SVector(), 0.0);
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