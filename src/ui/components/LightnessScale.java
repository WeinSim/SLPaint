package ui.components;

import main.ColorPicker;
import main.apps.App;
import sutil.ui.UIContainer;
import sutil.ui.UIScale;

public class LightnessScale extends UIScale {

    private ColorPicker colorPicker;

    public LightnessScale(int orientation, ColorPicker colorPicker) {
        super(orientation,
                () -> {
                    double value = App.isHSLColorSpace()
                            ? colorPicker.getLightness()
                            : colorPicker.getValue();
                    return (orientation == UIContainer.VERTICAL)
                            ? (1 - value)
                            : value;
                },
                value -> {
                    if (orientation == UIContainer.VERTICAL) {
                        value = 1 - value;
                    }
                    if (App.isHSLColorSpace()) {
                        colorPicker.setLightness(value);
                    } else {
                        colorPicker.setValue(value);
                    }
                });
        this.colorPicker = colorPicker;
    }

    public double getHue() {
        return colorPicker.getHue();
    }

    public double getSaturation() {
        return colorPicker.getHSLSaturation();
    }
}