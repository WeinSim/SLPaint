package sutil.ui;

public class KeyboardShortcut {

    private final String identifier;
    private final int key;
    private final int modifiers;
    private final UserAction action;

    public KeyboardShortcut(String identifier, int key, int modifiers, UserAction action) {
        this.identifier = identifier;
        this.key = key;
        this.modifiers = modifiers;
        this.action = action;
    }

    public boolean isPossible() {
        return action.isPossible();
    }

    public void keyPressed(int key, int modifiers) {
        if (this.key == key && this.modifiers == modifiers)
            action.run();
    }

    public void run() {
        action.run();
    }

    public String getIdentifier() {
        return identifier;
    }

    public int getKey() {
        return key;
    }

    public int getModifiers() {
        return modifiers;
    }
}