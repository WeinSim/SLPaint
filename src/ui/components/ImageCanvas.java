package ui.components;

import org.lwjgl.glfw.GLFW;

import main.apps.MainApp;
import main.tools.DragTool;
import sutil.math.SVector;
import sutil.ui.UIContainer;
import sutil.ui.UIElement;

public class ImageCanvas extends UIContainer {

    private MainApp app;

    public ImageCanvas(int orientation, int hAlignment, int vAlignment, MainApp app) {
        super(orientation, hAlignment, vAlignment);

        this.app = app;

        app.setCanvas(this);
        setFillSize();

        add(new TextToolContainer(app));
        add(new SelectionToolContainer(app));

        setLeftClickAction(() -> mousePressed(GLFW.GLFW_MOUSE_BUTTON_LEFT));
        setRightClickAction(() -> mousePressed(GLFW.GLFW_MOUSE_BUTTON_RIGHT));
    }

    private void mousePressed(int mouseButton) {
        // force quit currently active tool (if any)
        for (UIElement child : getChildren()) {
            if (child instanceof ToolContainer toolContainer) {
                toolContainer.getTool().finish();
            }
        }

        // TODO: unify these two
        app.toolClick(mouseButton);

        for (UIElement child : getChildren()) {
            if (child instanceof ToolContainer toolContainer) {
                if (toolContainer.tool == app.getActiveTool()) {
                    toolContainer.startInitialDrag();
                }
            }
        }
    }

    @Override
    public void positionChildren() {
        for (UIElement child : getChildren()) {
            if (child instanceof ToolContainer toolContainer) {
                DragTool tool = toolContainer.getTool();
                SVector pos = new SVector(tool.getX(), tool.getY());
                pos = app.getScreenPosition(pos);
                toolContainer.getPosition().set(pos);
            }
        }
    }
}