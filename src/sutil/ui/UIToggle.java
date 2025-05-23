package sutil.ui;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class UIToggle extends UIElement {

    private Supplier<Boolean> stateSupplier;

    public UIToggle(Supplier<Boolean> stateSupplier, Consumer<Boolean> stateSetter) {
        this.stateSupplier = stateSupplier;

        setLeftClickAction(() -> stateSetter.accept(!stateSupplier.get()));
    }

    @Override
    public void setPreferredSize() {
        double textSize = panel.getDefaultTextSize();
        size.set(3 * textSize , 1.5 * textSize);
    }

    public boolean getState() {
        return stateSupplier.get();
    }
}