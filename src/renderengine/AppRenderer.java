package renderengine;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL30;
import org.lwjglx.util.vector.Vector4f;

import main.Image;
import main.apps.App;
import main.apps.MainApp;
import renderengine.bufferobjects.FrameBufferObject;
import renderengine.fonts.TextFont;
import sutil.math.SVector;
import sutil.ui.UI;
import sutil.ui.UIColors;
import sutil.ui.UIContainer;
import sutil.ui.UIElement;
import sutil.ui.UIFloatContainer;
import sutil.ui.UIImage;
import sutil.ui.UIShape;
import sutil.ui.UISizes;
import sutil.ui.UIText;
import sutil.ui.UITextInput;
import ui.components.AlphaScale;
import ui.components.HueSatField;
import ui.components.LightnessScale;

public class AppRenderer {

    /**
     * 
     * <p>
     * The entire depth range ([-1.0, 1.0]) is divided into NUM_LAYERS layers. These
     * correspond to the ui layers.
     * </p>
     * <p>
     * Each layer is divided into NUM_DIVISIONS divisions. These correspond to
     * different nesting depths of the ui.
     * </p>
     * <p>
     * Each division is divided into NUM_SUBDIVISIONS subdivisions. These correspond
     * to different parts of one ui element, e.g. background and outline.
     * </p>
     */
    private static final int NUM_SUBDIVISIONS = 4;
    private static final int NUM_DIVISIONS = 1024;
    private static final int NUM_LAYERS = 16;

    private static final boolean DEBUG_RENDERING = false;

    protected App app;

    protected UIRenderMaster uiMaster;

    protected int layer;
    protected int division;

    public AppRenderer(App app) {
        this.app = app;
        uiMaster = new UIRenderMaster(app, app.getLoader());
    }

    public void render() {
        uiMaster.setBGColor(new Vector4f(0, 0, 0, 1));

        uiMaster.start();

        if (DEBUG_RENDERING) {
            renderDebug();
        } else {
            renderUI();
        }

        uiMaster.render();
    }

    protected void setDefaultBGColor() {
        uiMaster.setBGColor(UIColors.BACKGROUND_NORMAL.get());
    }

    protected void renderUI() {
        uiMaster.resetMatrix();

        layer = 0;
        division = 0;

        renderUIElement(UI.getRoot());
    }

    /**
     * Subdivisions:
     * Divisions incrementing from the bottom:
     * 0: checkerboard background
     * 1: background color
     * 2: main content (text, toggle, hueSat etc.)
     * Divisions decrementing from the top:
     * 0: stroke
     */
    private void renderUIElement(UIElement element) {
        // System.out.format("Rendering UIElement \"%s\", position = %s, size = %s\n",
        // element.getClass().getName(), element.getPosition().toString(),
        // element.getSize().toString());

        int oldLayer = layer;
        int oldDivision = division;

        SVector position = element.getPosition();
        SVector size = element.getSize();
        UIShape shape = element.shape();

        boolean ignoreClipArea = element instanceof UIFloatContainer f && f.ignoreClipArea();
        if (ignoreClipArea) {
            uiMaster.pushClipArea();
            uiMaster.noClipArea();
        }

        layer += element.getRelativeLayer();

        // checkerboard background
        if (element.doBackgroundCheckerboard()) {
            uiMaster.depth(getDepth(0));
            Vector4f c1 = element.backgroundCheckerboardColor1(),
                    c2 = element.backgroundCheckerboardColor2();
            double s = element.backgroundCheckerboardSize();
            uiMaster.checkerboardFill(new Vector4f[] { c1, c2 }, s);
            uiMaster.noStroke();
            drawShape(shape, position, size);

            // TODO: This is just a hack for now to guarantee that semi transparent colors
            // render correctly
            uiMaster.render();
        }

        // background
        Vector4f bgColor = element.backgroundColor();
        if (bgColor != null) {
            uiMaster.depth(getDepth(1));
            uiMaster.fill(bgColor);
            uiMaster.noStroke();
            drawShape(shape, position, size);
        }

        // outline
        boolean doOutline = false;
        int debugOutline = App.getDebugOutline();
        if ((debugOutline == 1 && element.mouseAbove()) || debugOutline == 2) {
            uiMaster.stroke(new SVector(1, 0.7, 0.1));
            uiMaster.strokeWeight(UISizes.STROKE_WEIGHT.get());
            doOutline = true;
        } else if (element.doStrokeCheckerboard()) {
            Vector4f c1 = element.strokeCheckerboardColor1(),
                    c2 = element.strokeCheckerboardColor2();
            double s = element.strokeCheckerboardSize();
            uiMaster.checkerboardStroke(new Vector4f[] { c1, c2 }, s);
            uiMaster.strokeWeight(element.strokeWeight());
            doOutline = true;
        } else {
            Vector4f olColor = element.strokeColor();
            if (olColor != null) {
                uiMaster.stroke(olColor);
                uiMaster.strokeWeight(element.strokeWeight());
                doOutline = true;
            }
        }
        if (doOutline) {
            // TODO: this is kind of an ugly hack to ensure that a container's outline
            // renders above all of its children.

            int oldOldDivision = division;

            division = NUM_DIVISIONS - 1 - division;

            uiMaster.depth(getDepth(0));
            uiMaster.noFill();
            uiMaster.rect(position, size);

            division = oldOldDivision;
        }

        // children
        if (element instanceof UIContainer container) {
            boolean isScrollable = container.isHScroll() || container.isVScroll();
            if (isScrollable) {
                uiMaster.pushClipArea();
                uiMaster.clipArea(position, size);
            }
            uiMaster.pushMatrix();
            uiMaster.translate(position);

            division++;
            for (UIElement child : container.getChildren()) {
                renderUIElement(child);
            }
            division--;

            uiMaster.popMatrix();
            if (isScrollable) {
                uiMaster.popClipArea();
            }
        }

        // content
        uiMaster.depth(getDepth(2));

        if (element instanceof UIText text) {
            String fontName = text.getFontName();
            double textSize = text.getTextSize();
            TextFont font = app.getLoader().loadFont(fontName);
            uiMaster.textFont(font);
            uiMaster.textSize(textSize);
            uiMaster.fill(text.getColor());
            uiMaster.text(text.getText(), position);
        }
        if (element instanceof UITextInput textInput) {
            if (textInput.isCursorVisible()) {
                uiMaster.noStroke();
                uiMaster.fill(textInput.strokeColor());
                uiMaster.rect(textInput.getCursorPosition(), textInput.getCursorSize());
            }
        }
        if (element instanceof UIImage image) {
            uiMaster.image(image.getTextureID(), position, size);
            // incredibly ugly but it works for now.
            // This causes the main image to render first and a (transparent) selection can
            // render above it. Otherwise, the selection would render first and the image
            // would not render behind transparent parts of the selection.
            uiMaster.render();
        }
        if (element instanceof LightnessScale.LSVisuals l) {
            uiMaster.lightnessScale(position, size, l.getHue(), l.getSaturation(),
                    l.getOrientation() == UIContainer.VERTICAL, App.isHSLColorSpace());
        }
        if (element instanceof AlphaScale.ASVisuals a) {
            // checkerboard background
            uiMaster.noStroke();
            Vector4f[] transparency = { UIColors.TRANSPARENCY_1.get(), UIColors.TRANSPARENCY_2.get() };
            uiMaster.checkerboardFill(transparency, size.y / 2);
            uiMaster.rect(position, size);
            // color gradient
            uiMaster.fill(MainApp.toVector4f(a.getRGB()));
            uiMaster.alphaScale(position, size, a.getOrientation() == UIContainer.VERTICAL);
        }
        if (element instanceof HueSatField) {
            uiMaster.hueSatField(position, size, App.isCircularHueSatField(),
                    App.isHSLColorSpace());
        }

        if (ignoreClipArea) {
            uiMaster.popClipArea();
        }

        layer = oldLayer;
        division = oldDivision;
    }

    private void drawShape(UIShape shape, SVector position, SVector size) {
        switch (shape) {
            case RECTANGLE -> uiMaster.rect(position, size);
            case ROUND_RECTANGLE -> {
                double max = Math.max(size.x, size.y),
                        min = Math.min(size.x, size.y);
                double difference = max - min;

                SVector ellipsePos1 = position,
                        ellipsePos2 = max == size.x
                                ? new SVector(position.x + size.x - min, position.y)
                                : new SVector(position.x, position.y + size.y - min);
                SVector ellipseSize = new SVector(min, min);

                SVector rectPos = max == size.x
                        ? new SVector(position.x + min / 2, position.y)
                        : new SVector(position.x, position.y + min / 2);
                SVector rectSize = max == size.x
                        ? new SVector(difference, min)
                        : new SVector(min, difference);

                uiMaster.ellipse(ellipsePos1, ellipseSize);
                uiMaster.ellipse(ellipsePos2, ellipseSize);
                uiMaster.rect(rectPos, rectSize);
            }
            case ELLIPSE -> uiMaster.ellipse(position, size);
        }
    }

    protected double getDepth(int subdivision) {
        int totalSubdiv = NUM_SUBDIVISIONS * (NUM_DIVISIONS * layer + division) + subdivision;
        double depth = -2 * (double) totalSubdiv / (NUM_LAYERS * NUM_DIVISIONS * NUM_SUBDIVISIONS) + 1;

        // System.out.format("getDepth: layer = %2d, division = %4d, subdivision = %d =>
        // %.4f\n",
        // layer, division, subdivision, depth);

        return depth;
    }

    public void reloadShaders() {
        uiMaster = new UIRenderMaster(app, app.getLoader());
    }

    public void renderImageToImage(Image srcImage, int x, int y, int width, int height, Image dstImage) {
        uiMaster.start();
        uiMaster.tempFrameBuffer();

        GL11.glDisable(GL11.GL_BLEND);

        uiMaster.setBGColor(new Vector4f(0, 0, 0, 0));
        uiMaster.image(srcImage.getTextureID(), new SVector(x, y), new SVector(width, height));

        uiMaster.render();

        GL11.glEnable(GL11.GL_BLEND);

        FrameBufferObject fbo = uiMaster.getTempFBO();
        int fboWidth = fbo.width(), fboHeight = fbo.height();

        int[] array = new int[fboWidth * fboHeight];
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, fbo.textureID());
        GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, array);

        dstImage.drawSubImage(0, 0, fboWidth, fboHeight, array);
    }

    public void renderTextToImage(String text, double x, double y, double size, Vector4f color, TextFont font,
            Image image) {

        if (text.isEmpty())
            return;

        uiMaster.start();
        uiMaster.tempFrameBuffer();

        // For this text rendering, we only care about the alpha output.
        // The color channel should be filled with the text color.
        GL30.glBlendFuncSeparate(
                GL11.GL_ONE, GL11.GL_ONE, // rgb
                GL11.GL_ONE_MINUS_DST_ALPHA, GL11.GL_ONE); // alpha

        uiMaster.setBGColor(new Vector4f(0, 0, 0, 0));
        uiMaster.fill(color);
        uiMaster.textFont(font);
        uiMaster.textSize(size);
        uiMaster.text(text, new SVector(x, y));

        uiMaster.render();

        FrameBufferObject fbo = uiMaster.getTempFBO();
        int width = fbo.width(), height = fbo.height();

        int[] array = new int[width * height];
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, fbo.textureID());
        GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, array);

        image.drawSubImage(0, 0, width, height, array);
    }

    /**
     * WARNING: this method expects the temp framebuffer to have a size of
     * {@code newWidth} x {@code newHeight}!
     */
    public void resizeImage(Image image, int newWidth, int newHeight) {
        uiMaster.start();
        uiMaster.tempFrameBuffer();

        GL11.glDisable(GL11.GL_BLEND);

        uiMaster.image(image.getTextureID(), new SVector(), new SVector(newWidth, newHeight));

        uiMaster.render();

        FrameBufferObject fbo = uiMaster.getTempFBO();
        int width = fbo.width(), height = fbo.height();

        int[] array = new int[width * height];
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, fbo.textureID());
        GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, array);

        image.resize(newWidth, newHeight, array);
    }

    public void setTempFBOSize(int width, int height) {
        uiMaster.setTempFBOSize(width, height);
    }

    private void renderDebug() {
        uiMaster.setBGColor(new Vector4f(0.15f, 0.15f, 0.15f, 1));

        SVector p1 = new SVector(500, 100),
                p2 = new SVector(600, 100),
                p3 = new SVector(1000, 500);
        SVector s1 = new SVector(800, 100),
                s2 = new SVector(800, 800),
                s3 = new SVector(100, 200);
        SVector c1 = new SVector(0.8, 0.2, 0.2),
                c2 = new SVector(0.2, 0.8, 0.2),
                c3 = new SVector(0.2, 0.2, 0.8);

        // MainApp app = (MainApp) this.app;

        // uiMaster.image(app.getImage().getTextureID(), p2, s2);

        // uiMaster.fill(c1);
        // uiMaster.ellipse(p1, s1);

        // uiMaster.fill(c2);
        // uiMaster.ellipse(p2, s2);

        // uiMaster.hueSatField(p3, new SVector(200, 200), true, true);

        uiMaster.noClipArea();

        uiMaster.noStroke();
        uiMaster.fill(c1);
        uiMaster.rect(p1, s1);

        uiMaster.noStroke();
        uiMaster.fill(c2);
        uiMaster.rect(p2, s2);

        uiMaster.noStroke();
        uiMaster.fill(c3);
        uiMaster.rect(p3, s3);

        // uiMaster.checkerboardFill(new Vector4f[] { new Vector4f(0, 0, 0, 1), new
        // Vector4f(1, 1, 1, 1) }, 15);
        // uiMaster.depth(getDepth(0));
        // uiMaster.rect(p2, s2);

        // p2.x += 50;

        // uiMaster.fill(c2);
        // uiMaster.depth(getDepth(1));
        // uiMaster.rect(p2, s2);

        // uiMaster.fill(c3);
        // uiMaster.rect(p3, s3);
    }
}