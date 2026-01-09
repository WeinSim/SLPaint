package main.tools;

import java.util.ArrayList;

import main.apps.MainApp;

/**
 * The {@code ImageTool} class is a subclass of {@code UIFloatContainer} such
 * that it can be placed inside the {@code ImageCanvas} and automatically get
 * the user inptus (key presses, mouse presses, mouse movement).
 */
public abstract sealed class ImageTool permits PencilTool, DragTool, FillBucketTool, PipetteTool {

    public static final PencilTool PENCIL = PencilTool.INSTANCE;
    public static final FillBucketTool FILL_BUCKET = FillBucketTool.INSTANCE;
    public static final PipetteTool PIPETTE = PipetteTool.INSTANCE;
    public static final SelectionTool SELECTION = SelectionTool.INSTANCE;
    public static final TextTool TEXT = TextTool.INSTANCE;

    public static final ImageTool[] INSTANCES = {
            PENCIL,
            FILL_BUCKET,
            PIPETTE,
            SELECTION,
            TEXT
    };

    public static final int NONE = 0x01;

    protected MainApp app;

    protected int state;

    private ArrayList<KeyboardShortcut> keyboardShortcuts;

    protected ImageTool() {
        state = NONE;

        keyboardShortcuts = new ArrayList<>();
    }

    public abstract void click(int x, int y, int mouseButton);

    public abstract void finish();

    public abstract String getName();

    public void setApp(MainApp app) {
        this.app = app;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    protected void addKeyboardShortcut(int key, int modifiers, int initialState, Runnable action) {
        keyboardShortcuts.add(new KeyboardShortcut(key, modifiers, initialState, action));
    }

    protected void addKeyboardShortcut(KeyboardShortcut shortcut) {
        keyboardShortcuts.add(shortcut);
    }

    public record KeyboardShortcut(int key, int modifiers, int initialState, Runnable action) {
    }

    public ArrayList<KeyboardShortcut> getKeyboardShortcuts() {
        return keyboardShortcuts;
    }
}