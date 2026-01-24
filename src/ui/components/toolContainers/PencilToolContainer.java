package ui.components.toolContainers;

import org.lwjgl.glfw.GLFW;

import main.apps.MainApp;
import main.tools.ImageTool;
import main.tools.PencilTool;

public final class PencilToolContainer extends ToolContainer<PencilTool> {

    public PencilToolContainer(MainApp app) {
        super(ImageTool.PENCIL, app);
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
    public void mouseReleased(int mouseButton, int mods) {
        switch (tool.getState()) {
            case PencilTool.DRAWING_PRIMARY -> {
                if (mouseButton == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                    tool.setState(PencilTool.NONE);
                }
            }
            case PencilTool.DRAWING_SECONDARY -> {
                if (mouseButton == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                    tool.setState(PencilTool.NONE);
                }
            }
        }
    }

    @Override
    protected int getVisibleStates() {
        return PencilTool.DRAWING_PRIMARY | PencilTool.DRAWING_SECONDARY;
    }
}