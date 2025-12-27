package ui.components;

import main.ColorPicker;
import sutil.ui.UIScale;

public class AlphaScale extends UIScale {

    private ColorPicker colorPicker;

    public AlphaScale(int orientation, ColorPicker colorPicker) {
        super(orientation, () -> colorPicker.getAlpha() / 255.0, d -> colorPicker.setAlpha((int) (d * 255)));
        this.colorPicker = colorPicker;

        narrow = false;
    }

    public int getRGB() {
        return colorPicker.getRGB();
    }
}