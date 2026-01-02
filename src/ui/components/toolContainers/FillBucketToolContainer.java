package ui.components.toolContainers;

import main.apps.MainApp;
import main.tools.FillBucketTool;
import main.tools.ImageTool;

public final class FillBucketToolContainer extends ToolContainer<FillBucketTool> {

    public FillBucketToolContainer(MainApp app) {
        super(ImageTool.FILL_BUCKET, app);
    }

    @Override
    protected int getVisibleStates() {
        return 0;
    }
}