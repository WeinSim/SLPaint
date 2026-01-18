package ui.components;

import main.ColorPicker;
import sutil.ui.UIScale;

public class AlphaScale extends UIScale {

    private ColorPicker colorPicker;

    public AlphaScale(int orientation, ColorPicker colorPicker) {
        super(orientation, () -> colorPicker.getAlpha() / 255.0, d -> colorPicker.setAlpha((int) (d * 255)), false);
        this.colorPicker = colorPicker;
    }

    @Override
    protected Visuals getVisuals(int orientation) {
        return new ASVisuals(orientation);
    }

    public class ASVisuals extends Visuals {

        public ASVisuals(int orientation) {
            super(orientation);
        }

        public int getRGB() {
            return colorPicker.getRGB();
        }
    }
}