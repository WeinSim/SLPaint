package main;

import main.apps.App;
import renderEngine.Window;
import sutil.SUtil;
import sutil.math.SVector;
import sutil.ui.UISetter;
import ui.components.DragTarget;
import ui.components.HueSatField;
import ui.components.UIScale;

public class ColorPicker {

    private App app;

    private UISetter<Integer> closeAction;

    private HueSatField hueSatField;
    private UIScale lightnessScale;
    private UIScale alphaScale;
    private DragTarget dragTarget;

    private int initialColor;
    private int rgb;
    private double hue;
    private double saturation;
    private double lightness;
    private double alpha;

    public ColorPicker(App app, int initialColor, UISetter<Integer> closeAction) {
        this.app = app;
        this.initialColor = initialColor;
        this.closeAction = closeAction;

        setRGB(initialColor);

        dragTarget = null;
    }

    public void update() {
        boolean[] mouseButtons = app.getWindow().getMouseButtons();
        if (!mouseButtons[0]) {
            dragTarget = null;
        }
        if (dragTarget != null) {
            dragTarget.drag();
        }

        updateCursorPositions();
    }

    public void setDragTarget(DragTarget dragTarget) {
        this.dragTarget = dragTarget;
    }

    public void setRGB(int rgb) {
        this.rgb = rgb;
        updateHSL();
    }

    public int getRGB() {
        return rgb;
    }

    private void updateRGB() {
        SVector v = SUtil.hslToRGB(hue, saturation, lightness);
        rgb = SUtil.toARGB(v.x, v.y, v.z, alpha);
    }

    public void setRed(int red) {
        setComponent(16, red);
    }

    public void setGreen(int green) {
        setComponent(8, green);
    }

    public void setBlue(int blue) {
        setComponent(0, blue);
    }

    public void setAlpha(int alpha) {
        setComponent(24, alpha);
    }

    public void setComponent(int shiftAmount, int component) {
        int mask = 0xFF << shiftAmount;
        rgb &= ~mask;
        rgb |= component << shiftAmount;
    }

    public void setHue(double hue) {
        this.hue = hue;
        updateRGB();
        // updateCursorPositions();
    }

    public void setSaturation(double saturation) {
        this.saturation = saturation;
        updateRGB();
        // updateCursorPositions();
    }

    public void setLightness(double lightness) {
        this.lightness = lightness;
        updateRGB();
        // updateCursorPositions();
    }

    public double getHue() {
        return hue;
    }

    public double getSaturation() {
        return saturation;
    }

    public double getLightness() {
        return lightness;
    }

    private void updateHSL() {
        SVector hsl = SUtil.rgbToHSL(SUtil.red(rgb), SUtil.green(rgb),
                SUtil.blue(rgb));
        hue = hsl.x;
        saturation = hsl.y;
        lightness = hsl.z;
        alpha = SUtil.alpha(rgb);

        // updateCursorPositions();
    }

    private void updateCursorPositions() {
        hueSatField.updateCursorPosition();
        lightnessScale.updateCursorPosition();
        if (alphaScale != null) {
            alphaScale.updateCursorPosition();
        }
    }

    public void setLightnessScale(UIScale lightnessScale) {
        this.lightnessScale = lightnessScale;
    }

    public void setAlphaScale(UIScale alphaScale) {
        this.alphaScale = alphaScale;
    }

    public void setHueSatField(HueSatField hueSatField) {
        this.hueSatField = hueSatField;
    }

    public int getInitialColor() {
        return initialColor;
    }

    public Window getWindow() {
        return app.getWindow();
    }

    public UISetter<Integer> getCloseAction() {
        return closeAction;
    }
}