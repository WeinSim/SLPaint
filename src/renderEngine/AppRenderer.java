package renderEngine;

import org.lwjgl.opengl.GL11;

import main.apps.App;
import main.apps.MainApp;
import sutil.SUtil;
import sutil.math.SVector;
import sutil.ui.UIContainer;
import sutil.ui.UIElement;
import sutil.ui.UIScrollArea;
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

    protected T app;

    protected UIRenderMaster uiMaster;

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
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
    }

    protected void renderUI() {
        uiMaster.resetMatrix();
        AppUI<?> ui = app.getUI();

        uiMaster.textFont(ui.getFont());
        uiMaster.textSize(ui.getTextSize());

        renderUIElement(ui.getRoot());
    }

    private void renderUIElement(UIElement element) {
        SVector position = element.getPosition();
        SVector size = element.getSize();

        SVector bgColor = element.getBackgroundColor();
        SVector olColor = element.getOutlineColor();

        if (element instanceof UIColorElement e && bgColor != null) {
            uiMaster.checkerboardFill(Colors.getTransparentColors(), 15);
            uiMaster.noStroke();
            uiMaster.rect(position, size);

            uiMaster.fillAlpha(SUtil.alpha(e.getColor()) / 255.0);
        }

        if (bgColor != null) {
            uiMaster.fill(bgColor);
        } else {
            uiMaster.noFill();
        }
        if (olColor != null) {
            uiMaster.strokeWeight(element.getStrokeWeight());
            uiMaster.stroke(olColor);
        } else {
            uiMaster.noStroke();
        }
        uiMaster.rect(position, size);

        uiMaster.fillAlpha(1.0);

        if (element instanceof HueSatField) {
            uiMaster.hueSatField(position, size, App.isCircularHueSatField());
        }
        if (element instanceof LightnessScale scale) {
            uiMaster.lightnessScale(position, size, scale.getHue(), scale.getSaturation(), scale.getOrientation());
        }
        if (element instanceof AlphaScale scale) {
            // checkerboard background
            uiMaster.noStroke();
            uiMaster.checkerboardFill(Colors.getTransparentColors(), size.y / 2);
            uiMaster.rect(position, size);

            // color gradient
            uiMaster.fill(MainApp.toSVector(scale.getRGB()));
            uiMaster.alphaScale(position, size, scale.getOrientation());
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
            uiMaster.pushMatrix();
            if (container instanceof UIScrollArea) {
                uiMaster.scissor(position, size);
            }
            uiMaster.translate(position);
            for (UIElement child : container.getChildren()) {
                renderUIElement(child);
            }
            if (container instanceof UIScrollArea) {
                uiMaster.noScissor();
            }
            uiMaster.popMatrix();
        }

        if (App.showDebugOutline()) {
            SVector orange = new SVector(1, 0.7, 0.1);
            uiMaster.stroke(orange);
            uiMaster.strokeWeight(Sizes.STROKE_WEIGHT.size);
            uiMaster.noFill();
            uiMaster.rect(position, size);
        }
    }

    public void reloadShaders() {
        uiMaster = new UIRenderMaster(app);
    }
}