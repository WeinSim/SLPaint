package main;

import sutil.SUtil;
import sutil.math.SVector;

public class ColorPicker {

    private int initialColor;
    private int rgb;

    private double hue;
    private double hslSaturation;
    private double hsvSaturation;
    private double lightness;
    private double value;

    private int alpha;

    public ColorPicker(int initialColor) {
        this.initialColor = initialColor;

        setRGB(initialColor);
    }

    public void setRGB(int rgb) {
        this.rgb = rgb;
        updateHSLFromRGB();
        updateHSVFromRGB();
    }

    public void setHSLHue(double hue) {
        this.hue = hue;
        updateRGBFromHSL();
        updateHSVFromHSL();
    }

    public void setHSVHue(double hue) {
        this.hue = hue;
        updateRGBFromHSV();
        updateHSLFromHSV();
    }

    public void setHSLSaturation(double hslSaturation) {
        this.hslSaturation = hslSaturation;
        updateRGBFromHSL();
        updateHSVFromHSL();
    }

    public void setHSVSaturation(double hsvSaturation) {
        this.hsvSaturation = hsvSaturation;
        updateRGBFromHSV();
        updateHSLFromHSV();
    }

    public void setLightness(double lightness) {
        this.lightness = lightness;
        updateRGBFromHSL();
        updateHSVFromHSL();
    }

    public void setValue(double value) {
        this.value = value;
        updateRGBFromHSV();
        updateHSLFromHSV();
    }

    public void setAlpha(int alpha) {
        setComponent(24, alpha);
        this.alpha = alpha;
    }

    public int getAlpha() {
        return alpha;
    }

    private void setComponent(int shiftAmount, int component) {
        int mask = 0xFF << shiftAmount;
        rgb &= ~mask;
        rgb |= component << shiftAmount;
    }

    public int getRGB() {
        return rgb;
    }

    public double getHue() {
        return hue;
    }

    public double getHSLSaturation() {
        return hslSaturation;
    }

    public double getHSVSaturation() {
        return hsvSaturation;
    }

    public double getLightness() {
        return lightness;
    }

    public double getValue() {
        return value;
    }

    private void updateRGBFromHSL() {
        SVector v = SUtil.hslToRGB(hue, hslSaturation, lightness);
        rgb = SUtil.toARGB(v.x, v.y, v.z, alpha);
    }

    private void updateRGBFromHSV() {
        SVector v = SUtil.hsvToRGB(hue, hsvSaturation, value);
        rgb = SUtil.toARGB(v.x, v.y, v.z, alpha);
    }

    private void updateHSLFromRGB() {
        SVector hsl = SUtil.rgbToHSL(SUtil.red(rgb), SUtil.green(rgb), SUtil.blue(rgb));
        hue = hsl.x;
        hslSaturation = hsl.y;
        lightness = hsl.z;
        alpha = SUtil.alpha(rgb);
    }

    private void updateHSLFromHSV() {
        SVector hsl = SUtil.hsvToHSL(hue, hsvSaturation, value);
        hue = hsl.x;
        hslSaturation = hsl.y;
        lightness = hsl.z;
    }

    private void updateHSVFromRGB() {
        SVector hsv = SUtil.rgbToHSV(SUtil.red(rgb), SUtil.green(rgb), SUtil.blue(rgb));
        hue = hsv.x;
        hsvSaturation = hsv.y;
        value = hsv.z;
        alpha = SUtil.alpha(rgb);
    }

    private void updateHSVFromHSL() {
        SVector hsv = SUtil.hslToHSV(hue, hslSaturation, lightness);
        hue = hsv.x;
        hsvSaturation = hsv.y;
        value = hsv.z;
    }

    public int getInitialColor() {
        return initialColor;
    }
}