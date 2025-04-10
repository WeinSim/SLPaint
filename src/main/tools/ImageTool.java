package main.tools;

import java.util.ArrayList;

import main.apps.MainApp;
import sutil.ui.UIAction;

public abstract sealed class ImageTool permits ClickTool, PencilTool, SelectionTool, TextTool {

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

    public static final int NONE = 0x01, INITIAL_DRAG = 0x02, IDLE = 0x04, IDLE_DRAG = 0x08;

    private ArrayList<KeyboardShortcut> keyboardShortcuts;

    protected MainApp app;

    private int state;

    private int mouseDragButton;

    protected ImageTool() {
        keyboardShortcuts = new ArrayList<>();
        state = NONE;
    }

    public static void init(MainApp app) {
        for (ImageTool tool : INSTANCES) {
            tool.app = app;
        }
    }

    public void start() {
        state = NONE;
    }

    public void mousePressed(int x, int y, int mouseButton) {
        if (state == NONE || state == IDLE) {
            boolean startDrag = state == NONE
                    ? startInitialDrag(x, y, mouseButton)
                    : startIdleDrag(x, y, mouseButton);
            if (startDrag) {
                if (state == NONE) {
                    state = INITIAL_DRAG;
                    handleInitialDrag(x, y, x, y);
                } else {
                    state = IDLE_DRAG;
                    handleIdleDrag(x, y, x, y);
                }
                mouseDragButton = mouseButton;
            } else {
                state = NONE;
            }
        }
    }

    public void mouseDragged(int x, int y, int px, int py, int mouseButton) {
        if (mouseButton != mouseDragButton) {
            return;
        }
        switch (state) {
            case INITIAL_DRAG -> {
                handleInitialDrag(x, y, px, py);
            }
            case IDLE_DRAG -> {
                handleIdleDrag(x, y, px, py);
            }
        }
    }

    public void mouseReleased(int mouseButton) {
        if (mouseButton != mouseDragButton) {
            return;
        }
        switch (state) {
            case INITIAL_DRAG -> {
                if (finishInitialDrag()) {
                    state = IDLE;
                } else {
                    state = NONE;
                }
            }
            case IDLE_DRAG -> {
                finishIdleDrag();
                state = IDLE;
            }
        }
    }

    public void keyPressed(int key, int modifiers) {
        for (KeyboardShortcut shortcut : keyboardShortcuts) {
            if (shortcut.key() == key
                    && (shortcut.initialState() & state) != 0
                    && shortcut.modifiers() == modifiers) {
                // final String baseString = "%s: executing shortcut. key = %d, modifiers = %d,
                // initialState = %d, finalState = %d\n";
                // System.out.format(baseString, getName(), shortcut.key(),
                // shortcut.modifiers(), shortcut.initialState(),
                // shortcut.finalState());
                app.setActiveTool(this);
                shortcut.action().run();
                state = shortcut.finalState;
                break;
            }
        }
    }

    public abstract void forceQuit();

    protected int getMouseDragButton() {
        return mouseDragButton;
    }

    public int getState() {
        return state;
    }

    /**
     * @return Wether the INITIAL_DRAG phase should be entered
     */
    protected abstract boolean startInitialDrag(int x, int y, int mouseButton);

    protected abstract void handleInitialDrag(int x, int y, int px, int py);

    /**
     * @return Wether the IDLE phase should be entered
     */
    protected abstract boolean finishInitialDrag();

    /**
     * @return Wether the IDLE_DRAG phase should be entered
     */
    protected abstract boolean startIdleDrag(int x, int y, int mouseButton);

    protected abstract void handleIdleDrag(int x, int y, int px, int py);

    protected abstract void finishIdleDrag();

    public abstract String getName();

    protected void addKeyboardShortcut(KeyboardShortcut shortcut) {
        keyboardShortcuts.add(shortcut);
    }

    protected record KeyboardShortcut(int key, int modifiers, int initialState, int finalState, UIAction action) {
    }
}