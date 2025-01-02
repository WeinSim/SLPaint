package ui.components;

import main.apps.MainApp;
import sutil.math.SVector;
import sutil.ui.UIElement;
import sutil.ui.UIGetter;
import sutil.ui.UIStyle;
import ui.Colors;

public class UIColorElement extends UIElement {

    private UIGetter<Integer> colorGetter;

    public UIColorElement(UIGetter<Integer> colorGetter, double wh, boolean outline) {
        this(colorGetter, new SVector(wh, wh), outline);
    }

    public UIColorElement(UIGetter<Integer> colorGetter, SVector size, boolean outline) {
        this.colorGetter = colorGetter;
        this.size = size;

        UIGetter<SVector> backgroundColorGetter = () -> MainApp.toSVector(getColor());
        UIGetter<SVector> outlineColorGetter = outline
                ? () -> getColor() == null
                        ? new SVector(0.5, 0.5, 0.5)
                        : Colors.getTextColor()
                : () -> null;
        UIGetter<Double> strokeWeightGetter = () -> 1.0;
        setStyle(new UIStyle(backgroundColorGetter, outlineColorGetter, strokeWeightGetter));
    }

    @Override
    public void setMinSize() {
    }

    public Integer getColor() {
        return colorGetter.get();
    }
}