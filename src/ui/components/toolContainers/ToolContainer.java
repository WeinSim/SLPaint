package ui.components.toolContainers;

import main.apps.MainApp;
import main.tools.ImageTool;
import sutil.ui.UIFloatContainer;

public abstract sealed class ToolContainer<T extends ImageTool> extends UIFloatContainer
        permits DragToolContainer, PencilToolContainer, PipetteToolContainer, FillBucketToolContainer {

    protected final T tool;

    protected MainApp app;

    public ToolContainer(T tool, MainApp app) {
        super(0, 0);

        this.tool = tool;
        this.app = app;
        tool.setApp(app);

        noOutline();
        noBackground();

        zeroPadding();
        zeroMargin();

        setVisibilitySupplier(() -> app.getActiveTool() == tool && (tool.getState() & getVisibleStates()) != 0);
    }

    protected abstract int getVisibleStates();
}