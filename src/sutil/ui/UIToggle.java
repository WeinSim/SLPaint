package sutil.ui;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class UIToggle extends UIElement {

    private Supplier<Boolean> stateGetter;

    public UIToggle(Supplier<Boolean> stateGetter, Consumer<Boolean> stateSetter) {
        this.stateGetter = stateGetter;
        // this.stateSetter = stateSetter;

        setLeftClickAction(() -> stateSetter.accept(!stateGetter.get()));
    }

    @Override
    public void setPreferredSize() {
        double textSize = panel.getTextSize();
        size.set(3 * textSize , 1.5 * textSize);
    }

    public boolean getState() {
        return stateGetter.get();
    }
}