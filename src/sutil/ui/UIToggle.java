package sutil.ui;

public class UIToggle extends UIElement {

    private UIGetter<Boolean> stateGetter;
    // private UISetter<Boolean> stateSetter;

    public UIToggle(UIGetter<Boolean> stateGetter, UISetter<Boolean> stateSetter) {
        this.stateGetter = stateGetter;
        // this.stateSetter = stateSetter;

        setClickAction(() -> stateSetter.set(!stateGetter.get()));
    }

    @Override
    public void setMinSize() {
        double textSize = panel.getTextSize();
        size.set(3 * textSize , 1.5 * textSize);
    }

    public boolean getState() {
        return stateGetter.get();
    }
}