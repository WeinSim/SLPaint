package sutil.ui;

import java.util.function.BooleanSupplier;

public class KeyboardShortcut extends UIKeyPressAction {

    private final String identifier;

    public KeyboardShortcut(String identifier, int key, int mods, BooleanSupplier possible, Runnable action) {
        super(key, mods, possible, action);
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    public boolean isPossible() {
        return possible.getAsBoolean();
    }

    // This code is duplicated from the constructor. But until some future update
    // (valhallah?) java won't let us use this::run in the constructor.
    public void run() {
        if (isPossible())
            action.run();
    }
}