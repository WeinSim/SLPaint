package ui.components;

import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

import org.lwjglx.util.vector.Vector4f;

import sutil.math.SVector;
import sutil.ui.UIColors;
import sutil.ui.UIElement;
import sutil.ui.UISizes;

public class UIColorElement extends UIElement {

    private Supplier<Vector4f> colorSupplier;
    private Supplier<SVector> sizeSupplier;

    public UIColorElement(Supplier<Vector4f> colorSupplier, DoubleSupplier sizeSupplier) {
        this(
                colorSupplier,
                () -> {
                    double wh = sizeSupplier.getAsDouble();
                    return new SVector(wh, wh);
                },
                true);
    }

    public UIColorElement(Supplier<Vector4f> colorSupplier, Supplier<SVector> sizeSupplier, boolean outline) {
        this.colorSupplier = colorSupplier;
        this.sizeSupplier = sizeSupplier;

        style.setBackgroundColor(this::getColor);
        style.setBackgroundCheckerboard(
                () -> getColor() != null,
                UIColors.TRANSPARENCY_1,
                UIColors.TRANSPARENCY_2,
                UISizes.CHECKERBOARD);
        if (outline) {
            style.setStrokeColor(() -> getColor() == null ? UIColors.INVALID.get() : UIColors.HIGHLIGHT.get());
        }
    }

    @Override
    public void setPreferredSize() {
        size.set(sizeSupplier.get());
    }

    public Vector4f getColor() {
        return colorSupplier.get();
    }
}