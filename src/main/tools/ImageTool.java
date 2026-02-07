package main.tools;

import main.apps.MainApp;
import sutil.ui.KeyboardShortcut;
import sutil.ui.UserAction;

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

    protected ImageTool() {
        state = NONE;
    }

    public abstract void createKeyboardShortcuts();

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

    protected void addShortcut(String identifier, int key, int modifiers,
            int possibleStates, Runnable action) {

        UserAction userAction = new UserAction(
                () -> {
                    app.setActiveTool(this);
                    action.run();
                },
                () -> (getState() & possibleStates) != 0);
        app.addKeyboardShortcut(new KeyboardShortcut(identifier, key, modifiers, userAction));
    }
}