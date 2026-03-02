package ui.components.toolContainers;

import static org.lwjgl.glfw.GLFW.*;

import main.apps.MainApp;
import main.tools.ImageTool;
import main.tools.PencilTool;

public final class PencilToolContainer extends ToolContainer<PencilTool> {

    public PencilToolContainer(MainApp app) {
        super(ImageTool.PENCIL, app);

        addMouseReleaseAction(GLFW_MOUSE_BUTTON_LEFT, false, () -> {
            if (tool.getState() == PencilTool.DRAWING_PRIMARY) {
                tool.setState(PencilTool.NONE);
                app.addImageSnapshot();
            }
        });
        addMouseReleaseAction(GLFW_MOUSE_BUTTON_RIGHT, false, () -> {
            if (tool.getState() == PencilTool.DRAWING_SECONDARY) {
                tool.setState(PencilTool.NONE);
                app.addImageSnapshot();
            }
        });
    }

    @Override
    public void update() {
        super.update();

        int state = tool.getState();
        if (state != PencilTool.NONE) {
            int[] mousePosition = app.getMouseImagePosition();
            int mouseX = mousePosition[0],
                    mouseY = mousePosition[1];

            int[] prevMousePosition = app.getPrevMouseImagePosition();
            int pmouseX = prevMousePosition[0],
                    pmouseY = prevMousePosition[1];

            int color = state == PencilTool.DRAWING_PRIMARY ? app.getPrimaryColor() : app.getSecondaryColor();
            app.drawLine(mouseX, mouseY, pmouseX, pmouseY, tool.getSize(), color);
        }
    }

    @Override
    protected int getVisibleStates() {
        return PencilTool.DRAWING_PRIMARY | PencilTool.DRAWING_SECONDARY;
    }
}