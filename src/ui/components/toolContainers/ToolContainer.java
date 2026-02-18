package ui.components.toolContainers;

import main.apps.MainApp;
import main.tools.ImageTool;
import sutil.ui.elements.UIFloatContainer;

public abstract sealed class ToolContainer<T extends ImageTool> extends UIFloatContainer
        permits DragToolContainer, PencilToolContainer, LineToolContainer, PipetteToolContainer,
        FillBucketToolContainer {

    protected final T tool;

    protected MainApp app;

    public ToolContainer(T tool, MainApp app) {
        super(0, 0);

        this.tool = tool;
        this.app = app;

        noOutline();
        noBackground();

        setVisibilitySupplier(() -> app.getActiveTool() == tool && (tool.getState() & getVisibleStates()) != 0);
    }

    protected abstract int getVisibleStates();
}