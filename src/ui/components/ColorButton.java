package ui.components;

import main.MainApp;
import sutil.math.SVector;
import sutil.ui.UIElement;
import sutil.ui.UIGetter;
import sutil.ui.UIStyle;

public class ColorButton extends UIElement {

    private UIGetter<Integer> colorGetter;
    private double wh;

    public ColorButton(UIGetter<Integer> colorGetter, double size) {
        this.colorGetter = colorGetter;
        wh = size;

        UIGetter<SVector> backgroundColorGetter = () -> MainApp.toSVector(getColor());
        UIGetter<SVector> outlineColorGetter = () -> getColor() == null
                ? new SVector(0.5, 0.5, 0.5)
                : new SVector(1, 1, 1);
        // UIGetter<SVector> outlineColorGetter = () -> new SVector(1, 1, 1);
        UIGetter<Double> strokeWeightGetter = () -> 1.0;
        setStyle(new UIStyle(backgroundColorGetter, outlineColorGetter, strokeWeightGetter));
    }

    @Override
    public void update(SVector mouse) {
        super.update(mouse);
    }

    @Override
    public void setMinSize() {
        size = new SVector(wh, wh);
    }

    public Integer getColor() {
        return colorGetter.get();
    }
}