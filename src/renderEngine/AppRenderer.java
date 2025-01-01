package renderEngine;

import org.lwjgl.opengl.GL11;

import main.apps.App;
import sutil.math.SVector;
import sutil.ui.UIContainer;
import sutil.ui.UIElement;
import sutil.ui.UIText;
import ui.AppUI;
import ui.components.HueSatField;
import ui.components.LightnessScaleContainer.LightnessScale;

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
        SVector bgColor = App.getBackgroundNormalColor();
        GL11.glClearColor((float) bgColor.x, (float) bgColor.y, (float) bgColor.z, 1);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
    }

    protected void renderUI() {
        uiMaster.resetMatrix();
        AppUI<?> ui = app.getUI();
        renderUI(ui);
    }

    private void renderUI(AppUI<?> ui) {
        uiMaster.textFont(ui.getFont());
        uiMaster.textSize(ui.getTextSize());

        renderUIElement(ui.getRoot());
    }

    private void renderUIElement(UIElement element) {
        SVector position = element.getPosition();
        SVector size = element.getSize();

        SVector bgColor = element.getBackgroundColor();
        SVector olColor = element.getOutlineColor();
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

        if (element instanceof HueSatField) {
            uiMaster.hueSatField(position, size);
        }
        if (element instanceof LightnessScale scale) {
            uiMaster.lightnessScale(position, size, scale.getHue(), scale.getSaturation());
        }
        if (element instanceof UIContainer container) {
            uiMaster.pushMatrix();
            uiMaster.translate(position);
            for (UIElement child : container.getChildren()) {
                renderUIElement(child);
            }
            uiMaster.popMatrix();
        }
        if (element instanceof UIText text) {
            uiMaster.fill(App.darkMode ? new SVector(1, 1, 1) : new SVector(0, 0, 0));
            uiMaster.text(text.getText(), position);
        }

        if (App.showDebugOutline()) {
            SVector orange = new SVector(1, 0.7, 0.1);
            uiMaster.stroke(orange);
            uiMaster.strokeWeight(1);
            uiMaster.noFill();
            uiMaster.rect(position, size);
        }
    }

    public void reloadShaders() {
        uiMaster = new UIRenderMaster(app);
    }
}