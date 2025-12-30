package main.tools;

import main.apps.MainApp;

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

    protected MainApp app;

    protected ImageTool() {
    }

    public static void init(MainApp app) {
        for (ImageTool tool : INSTANCES) {
            tool.app = app;
        }
    }

    public void finish() {

    }

    public abstract void click(int x, int y, int mouseButton);

    public abstract String getName();
}