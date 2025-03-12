package sutil.ui;

import java.util.function.Supplier;

import sutil.math.SVector;

public class UIStyle {

    protected Supplier<SVector> backgroundColorGetter;
    protected Supplier<SVector> outlineColorGetter;
    protected Supplier<Double> strokeWeightGetter;

    public UIStyle(Supplier<SVector> backgroundColorGetter, Supplier<SVector> outlineColorGetter,
            Supplier<Double> strokeWeightGetter) {
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

    public void setBackgroundColorGetter(Supplier<SVector> backgroundColorGetter) {
        this.backgroundColorGetter = backgroundColorGetter;
    }

    public void setOutlineColorGetter(Supplier<SVector> outlineColorGetter) {
        this.outlineColorGetter = outlineColorGetter;
    }

    public void setStrokeWeightGetter(Supplier<Double> strokeWeightGetter) {
        this.strokeWeightGetter = strokeWeightGetter;
    }
}