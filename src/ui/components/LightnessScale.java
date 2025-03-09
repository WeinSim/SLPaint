package ui.components;

import main.ColorPicker;

public class LightnessScale extends UIScale {

    private ColorPicker colorPicker;

    public LightnessScale(int orientation, ColorPicker colorPicker) {
        super(orientation, () -> 1 - colorPicker.getLightness(), d -> colorPicker.setLightness(1 - d));
        this.colorPicker = colorPicker;
    }

    public double getHue() {
        return colorPicker.getHue();
    }

    public double getSaturation() {
        return colorPicker.getSaturation();
    }
}