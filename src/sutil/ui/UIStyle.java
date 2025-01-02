package sutil.ui;

import sutil.math.SVector;

public class UIStyle {

    protected UIGetter<SVector> backgroundColorGetter;
    protected UIGetter<SVector> outlineColorGetter;
    protected UIGetter<Double> strokeWeightGetter;

    public UIStyle(UIGetter<SVector> backgroundColorGetter, UIGetter<SVector> outlineColorGetter,
            UIGetter<Double> strokeWeightGetter) {
        this.backgroundColorGetter = backgroundColorGetter;
        this.outlineColorGetter = outlineColorGetter;
        this.strokeWeightGetter = strokeWeightGetter;
    }

    public SVector getBackgroundColor() {
        return backgroundColorGetter.get();
    }

    public SVector getOutlineColor() {
        return outlineColorGetter.get();
    }

    public double getStrokeWeight() {
        return strokeWeightGetter.get();
    }

    public void setBackgroundColorGetter(UIGetter<SVector> backgroundColorGetter) {
        this.backgroundColorGetter = backgroundColorGetter;
    }

    public void setOutlineColorGetter(UIGetter<SVector> outlineColorGetter) {
        this.outlineColorGetter = outlineColorGetter;
    }

    public void setStrokeWeightGetter(UIGetter<Double> strokeWeightGetter) {
        this.strokeWeightGetter = strokeWeightGetter;
    }
}