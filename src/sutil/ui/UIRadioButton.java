package sutil.ui;

import java.util.function.Supplier;

public class UIRadioButton extends UIElement {

    private Supplier<Integer> stateSupplier;
    private int index;

    public UIRadioButton(int index, Supplier<Integer> stateSupplier) {
        this.index = index;
        this.stateSupplier = stateSupplier;
    }

    @Override
    public void setPreferredSize() {
        double wh = panel.defaultTextSize() * 1.3;
        size.set(wh, wh);
    }

    public boolean getState() {
        return stateSupplier.get() == index;
    }
}