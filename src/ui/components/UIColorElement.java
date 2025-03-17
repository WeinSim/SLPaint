package ui.components;

import java.util.function.Supplier;

import main.apps.MainApp;
import sutil.math.SVector;
import sutil.ui.UIElement;
import sutil.ui.UIStyle;
import ui.Colors;
import ui.Sizes;

public class UIColorElement extends UIElement {

    private Supplier<Integer> colorGetter;

    public UIColorElement(Supplier<Integer> colorGetter, double wh, boolean outline) {
        this(colorGetter, new SVector(wh, wh), outline);
    }

    public UIColorElement(Supplier<Integer> colorGetter, SVector size, boolean outline) {
        this.colorGetter = colorGetter;
        this.size = size;

        Supplier<SVector> backgroundColorGetter = () -> MainApp.toSVector(getColor());
        Supplier<SVector> outlineColorGetter = outline
                ? () -> getColor() == null
                        ? new SVector(0.5, 0.5, 0.5)
                        : Colors.getTextColor()
                // : Colors.getOutlineNormalColor()
                : () -> null;
        Supplier<Double> strokeWeightGetter = () -> Sizes.STROKE_WEIGHT.size;
        setStyle(new UIStyle(backgroundColorGetter, outlineColorGetter, strokeWeightGetter));
    }

    @Override
    public void setPreferredSize() {
    }

    public Integer getColor() {
        return colorGetter.get();
    }
}