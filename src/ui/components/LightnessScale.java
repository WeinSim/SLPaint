package ui.components;

import main.ColorPicker;
import sutil.math.SVector;
import sutil.ui.UIContainer;

public class LightnessScale extends UIScale {

    public LightnessScale(int orientation, ColorPicker colorPicker) {
        super(orientation, colorPicker);

        colorPicker.setLightnessScale(this);
    }

    @Override
    public void setColorDimension(SVector mousePos) {
        if (orientation == VERTICAL) {
            colorPicker.setLightness(1 - mousePos.y / size.y);
        } else {
            colorPicker.setLightness(mousePos.x / size.x);
        }
    }

    @Override
    protected double getSliderCoord() {
        return orientation == UIContainer.VERTICAL
                ? (1 - colorPicker.getLightness()) * size.y
                : colorPicker.getLightness() * size.x;
    }

    public double getHue() {
        return colorPicker.getHue();
    }

    public double getSaturation() {
        return colorPicker.getSaturation();
    }
}