package ui.components;

import java.util.function.Supplier;

import main.apps.MainApp;
import sutil.math.SVector;
import sutil.ui.UIElement;
import sutil.ui.UIStyle;
import ui.Colors;
import ui.Sizes;

public class UIColorElement extends UIElement {

    private Supplier<Integer> colorSupplier;

    public UIColorElement(Supplier<Integer> colorSupplier, double wh, boolean outline) {
        this(colorSupplier, new SVector(wh, wh), outline);
    }

    public UIColorElement(Supplier<Integer> colorSupplier, SVector size, boolean outline) {
        this.colorSupplier = colorSupplier;
        this.size = size;

        Supplier<SVector> backgroundColorSupplier = () -> MainApp.toSVector(getColor());
        Supplier<SVector> outlineColorSupplier = outline
                ? () -> getColor() == null
                        ? new SVector(0.5, 0.5, 0.5)
                        : Colors.getTextColor()
                // : Colors.getOutlineNormalColor()
                : () -> null;
        Supplier<Double> strokeWeightSupplier = () -> Sizes.STROKE_WEIGHT.size;
        setStyle(new UIStyle(backgroundColorSupplier, outlineColorSupplier, strokeWeightSupplier));
    }

    @Override
    public void setPreferredSize() {
    }

    public Integer getColor() {
        return colorSupplier.get();
    }
}