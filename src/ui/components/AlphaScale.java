package ui.components;

import main.ColorPicker;
import sutil.SUtil;
import sutil.math.SVector;

public class AlphaScale extends UIScale {

    public AlphaScale(int orientation, ColorPicker colorPicker) {
        super(orientation, colorPicker);

        colorPicker.setAlphaScale(this);
    }

    @Override
    public void setColorDimension(SVector mousePos) {
        double fraction = orientation == VERTICAL
                ? 1 - mousePos.y / size.y
                : mousePos.x / size.x;
        int alpha = Math.min(Math.max(0, (int) (255 * fraction)), 255);
        colorPicker.setAlpha(alpha);
    }

    @Override
    public double getSliderCoord() {
        return orientation == VERTICAL
                ? (1 - SUtil.alpha(colorPicker.getRGB()) / 255.0) * size.y
                : SUtil.alpha(colorPicker.getRGB()) / 255.0 * size.x;
    }

    public int getRGB() {
        return colorPicker.getRGB();
    }

    public double getHue() {
        return colorPicker.getHue();
    }

    public double getSaturation() {
        return colorPicker.getHSLSaturation();
    }
}