package renderEngine;

import org.lwjgl.opengl.GL11;

import main.apps.App;
import main.apps.MainApp;
import sutil.SUtil;
import sutil.math.SVector;
import sutil.ui.UIContainer;
import sutil.ui.UIElement;
import sutil.ui.UIFloatContainer;
import sutil.ui.UIText;
import sutil.ui.UIToggle;
import ui.AppUI;
import ui.Colors;
import ui.Sizes;
import ui.components.AlphaScale;
import ui.components.HueSatField;
import ui.components.LightnessScale;
import ui.components.UIColorElement;
import ui.components.UIScale;

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
        setBGColor(Colors.getBackgroundNormalColor());
    }

    protected void setBGColor(SVector bgColor) {
        GL11.glClearColor((float) bgColor.x, (float) bgColor.y, (float) bgColor.z, 1);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
    }

    protected void renderUI() {
        uiMaster.resetMatrix();
        AppUI<?> ui = app.getUI();

        uiMaster.textFont(ui.getFont());
        uiMaster.textSize(ui.getTextSize());

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
            uiMaster.fill(Colors.getTextColor());
            uiMaster.text(text.getText(), position);
        }
        if (element instanceof UIContainer container) {
            boolean isScrollable = container.isHScroll() || container.isVScroll();
            // SVector pos = new SVector(position),
            // siz = new SVector(size);
            // double extra = 1;
            // pos.x -= extra;
            // pos.y -= extra;
            // siz.x += 2 * extra;
            // siz.y += 2 * extra;
            // isScrollable = true;
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
                uiMaster.lightnessScale(pos, siz, l.getHue(), l.getSaturation(), l.getOrientation(), App.isHSLColorSpace());
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
}