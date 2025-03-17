package ui.components;

import main.ColorPicker;
import main.apps.App;
import sutil.math.SVector;
import sutil.ui.UIContainer;

public class LightnessScale extends UIScale {

    public LightnessScale(int orientation, ColorPicker colorPicker) {
        super(orientation, colorPicker);

        colorPicker.setLightnessScale(this);
    }

    @Override
    public void setColorDimension(SVector mousePos) {
        double value = orientation == VERTICAL
                ? 1 - mousePos.y / size.y
                : mousePos.x / size.x;
        if (App.isHSLColorSpace()) {
            colorPicker.setLightness(value);
        } else {
            colorPicker.setValue(value);
        }
    }

    @Override
    protected double getSliderCoord() {
        double value = App.isHSLColorSpace()
                ? colorPicker.getLightness()
                : colorPicker.getValue();
        return orientation == UIContainer.VERTICAL
                ? (1 - value) * size.y
                : value * size.x;
    }

    public double getHue() {
        return colorPicker.getHue();
    }

    public double getSaturation() {
        return colorPicker.getHSLSaturation();
    }
}