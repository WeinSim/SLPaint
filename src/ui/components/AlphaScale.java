package ui.components;

import main.ColorPicker;

public class AlphaScale extends UIScale {

    private ColorPicker colorPicker;

    public AlphaScale(int orientation, ColorPicker colorPicker) {
        super(orientation, () -> colorPicker.getAlpha() / 255.0, d -> colorPicker.setAlpha((int) (d * 255)));
        this.colorPicker = colorPicker;
    }

    public int getRGB() {
        return colorPicker.getRGB();
    }
}