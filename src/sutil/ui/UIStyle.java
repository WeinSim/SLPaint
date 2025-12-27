package sutil.ui;

import java.util.function.Supplier;

import sutil.math.SVector;

public class UIStyle {

    protected Supplier<SVector> backgroundColorSupplier;
    protected Supplier<SVector> outlineColorSupplier;
    protected Supplier<Double> strokeWeightSupplier;

    public UIStyle(Supplier<SVector> backgroundColorSupplier, Supplier<SVector> outlineColorSupplier,
            Supplier<Double> strokeWeightSupplier) {

        this.backgroundColorSupplier = backgroundColorSupplier;
        this.outlineColorSupplier = outlineColorSupplier;
        this.strokeWeightSupplier = strokeWeightSupplier;
    }

    public SVector getBackgroundColor() {
        return backgroundColorSupplier.get();
    }

    public SVector getOutlineColor() {
        return outlineColorSupplier.get();
    }

    public double getStrokeWeight() {
        return strokeWeightSupplier.get();
    }

    public void setBackgroundColorSupplier(Supplier<SVector> backgroundColorSupplier) {
        this.backgroundColorSupplier = backgroundColorSupplier;
    }

    public void setOutlineColorSupplier(Supplier<SVector> outlineColorSupplier) {
        this.outlineColorSupplier = outlineColorSupplier;
    }

    public void setStrokeWeightSupplier(Supplier<Double> strokeWeightSupplier) {
        this.strokeWeightSupplier = strokeWeightSupplier;
    }
}