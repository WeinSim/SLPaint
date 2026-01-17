package sutil.ui;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class UIToggle extends UIElement {

    private Supplier<Boolean> stateSupplier;

    public UIToggle(Supplier<Boolean> stateSupplier, Consumer<Boolean> stateConsumer) {
        this.stateSupplier = stateSupplier;

        setLeftClickAction(() -> stateConsumer.accept(!stateSupplier.get()));
    }

    @Override
    public void setPreferredSize() {
        double textSize = panel.get(UISizes.TEXT);
        size.set(3 * textSize , 1.5 * textSize);
    }

    public boolean getState() {
        return stateSupplier.get();
    }
}