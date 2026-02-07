package sutil.ui.elements;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class UIDropdown extends UIMenuButton {

    public UIDropdown(String[] options, IntSupplier valueSupplier, IntConsumer valueSetter) {
        this(options, valueSupplier, valueSetter, false);
    }

    public UIDropdown(String[] options, IntSupplier valueSupplier, IntConsumer valueSetter,
            boolean scroll) {

        super(() -> options[valueSupplier.getAsInt()], scroll);

        for (int i = 0; i < options.length; i++) {
            final int j = i;
            addLabel(options[i], () -> valueSetter.accept(j));
        }
    }
}