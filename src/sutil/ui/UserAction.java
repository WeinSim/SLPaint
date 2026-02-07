package sutil.ui;

import java.util.function.BooleanSupplier;

public class UserAction {

    private final Runnable action;
    private final BooleanSupplier isPossible;

    public UserAction(Runnable action, BooleanSupplier isPossible) {
        this.action = action;
        this.isPossible = isPossible;
    }

    /**
     * This method can always safely be called.
     */
    public void run() {
        if (isPossible())
            action.run();
    }

    public boolean isPossible() {
        return isPossible.getAsBoolean();
    }
}