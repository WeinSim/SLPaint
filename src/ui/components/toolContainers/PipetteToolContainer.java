package ui.components.toolContainers;

import main.apps.MainApp;
import main.tools.ImageTool;
import main.tools.PipetteTool;

public final class PipetteToolContainer extends ToolContainer<PipetteTool> {

    public PipetteToolContainer(MainApp app) {
        super(ImageTool.PIPETTE, app);
    }

    @Override
    protected int getVisibleStates() {
        return 0;
    }
}