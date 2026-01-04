package ui.components;

import java.util.function.Supplier;

import org.lwjglx.util.vector.Vector4f;

import main.apps.MainApp;
import sutil.math.SVector;
import sutil.ui.UIElement;
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

        style.setBackgroundColor(() -> MainApp.toVector4f(getColor()));
        style.setBackgroundCheckerboard(
                () -> getColor() != null,
                () -> Colors.transparent()[0],
                () -> Colors.transparent()[1],
                () -> Sizes.CHECKERBOARD_SIZE.size);
        if (outline) {
            style.setStrokeColor(() -> getColor() == null ?  new Vector4f(0.5f, 0.5f, 0.5f, 1.0f) : Colors.text());
        }
    }

    @Override
    public void setPreferredSize() {
    }

    public Integer getColor() {
        return colorSupplier.get();
    }
}