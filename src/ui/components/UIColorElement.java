package ui.components;

import java.util.function.Supplier;

import org.lwjglx.util.vector.Vector4f;

import main.apps.MainApp;
import sutil.math.SVector;
import sutil.ui.UIElement;
import ui.AppUI;
import ui.Colors;
import ui.Sizes;

public class UIColorElement extends UIElement {

    private Supplier<Integer> colorSupplier;
    private Supplier<SVector> sizeSupplier;

    public UIColorElement(Supplier<Integer> colorSupplier, Supplier<Double> sizeSupplier) {
        this(
                colorSupplier,
                () -> {
                    double wh = sizeSupplier.get();
                    return new SVector(wh, wh);
                },
                true);
    }

    public UIColorElement(Supplier<Integer> colorSupplier, Supplier<SVector> sizeSupplier, boolean outline) {
        this.colorSupplier = colorSupplier;
        this.sizeSupplier = sizeSupplier;

        style.setBackgroundColor(() -> MainApp.toVector4f(getColor()));
        style.setBackgroundCheckerboard(
                () -> getColor() != null,
                () -> Colors.transparent()[0],
                () -> Colors.transparent()[1],
                () -> ((AppUI<?>) panel).getSize(Sizes.CHECKERBOARD));
        if (outline) {
            style.setStrokeColor(() -> getColor() == null ? new Vector4f(0.5f, 0.5f, 0.5f, 1.0f) : Colors.text());
        }
    }

    @Override
    public void setPreferredSize() {
        size.set(sizeSupplier.get());
    }

    public Integer getColor() {
        return colorSupplier.get();
    }
}