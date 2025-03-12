package main;

import java.util.function.Consumer;

import main.apps.App;
import renderEngine.Window;
import sutil.SUtil;
import sutil.math.SVector;

public class ColorPicker {

    private App app;

    private Consumer<Integer> closeAction;

    private int initialColor;
    private int rgb;
    private double hue;
    private double saturation;
    private double lightness;
    private int alpha;

    public ColorPicker(App app, int initialColor, Consumer<Integer> closeAction) {
        this.app = app;
        this.initialColor = initialColor;
        this.closeAction = closeAction;

        setRGB(initialColor);
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
        this.alpha = alpha;
    }

    public int getAlpha() {
        return alpha;
    }

    public void setComponent(int shiftAmount, int component) {
        int mask = 0xFF << shiftAmount;
        rgb &= ~mask;
        rgb |= component << shiftAmount;
    }

    public void setHue(double hue) {
        this.hue = hue;
        updateRGB();
    }

    public void setSaturation(double saturation) {
        this.saturation = saturation;
        updateRGB();
    }

    public void setLightness(double lightness) {
        this.lightness = lightness;
        updateRGB();
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
    }

    public int getInitialColor() {
        return initialColor;
    }

    public Window getWindow() {
        return app.getWindow();
    }

    public Consumer<Integer> getCloseAction() {
        return closeAction;
    }
}