package sutil.ui;

import java.util.function.BooleanSupplier;

/**
 * The {@code selected} field controls wether this UIElement has to be selected
 * in order for this key press action to run.
 */
public class UIKeyPressAction {

    private static final BooleanSupplier TRUE = () -> true;

    protected final int key;
    protected final int mods;
    protected final boolean selected;
    protected final BooleanSupplier possible;
    protected final Runnable action;

    public UIKeyPressAction(int key, int mods, Runnable action) {
        this(key, mods, true, TRUE, action);
    }

    public UIKeyPressAction(int key, int mods, boolean selected, Runnable action) {
        this(key, mods, selected, TRUE, action);
    }

    public UIKeyPressAction(int key, int mods, BooleanSupplier possible, Runnable action) {
        this(key, mods, true, possible, action);
    }

    public UIKeyPressAction(int key, int mods, boolean selected, BooleanSupplier possible, Runnable action) {
        this.key = key;
        this.mods = mods;
        this.selected = selected;
        this.possible = possible;
        this.action = action;
    }

    public void keyPressed(int key, int mods, boolean isSelected) {
        if ((!selected || isSelected) && isPossible() && this.key == key && this.mods == mods)
            action.run();
    }

    public int getKey() {
        return key;
    }

    public int getModifiers() {
        return mods;
    }

    public boolean isPossible() {
        return possible.getAsBoolean();
    }
}