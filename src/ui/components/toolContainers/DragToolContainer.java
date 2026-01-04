package ui.components.toolContainers;

import org.lwjgl.glfw.GLFW;

import main.apps.MainApp;
import main.tools.DragTool;
import sutil.math.SVector;
import ui.Colors;
import ui.Sizes;

public abstract sealed class DragToolContainer<T extends DragTool> extends ToolContainer<T>
        permits SelectionToolContainer, TextToolContainer {

    // IDLE_DRAG
    private SVector dragStartMouseCoords;
    private int dragStartX, dragStartY;

    public DragToolContainer(T tool, MainApp app) {
        super(tool, app);

        clipToRoot = false;
        relativeLayer = 2;

        style.setStrokeCheckerboard(
                () -> Colors.selectionBorder()[0],
                () -> Colors.selectionBorder()[1],
                () -> Sizes.CHECKERBOARD_SIZE.size);
        style.setStrokeWeight(2.0);

        setLeftClickAction(this::startIdleDrag);

        setMarginScale(1.0);
    }

    @Override
    public void update() {
        super.update();

        switch (tool.getState()) {
            case DragTool.INITIAL_DRAG -> {
                int[] mouseImagePos = app.getMouseImagePosition();
                int mouseX = mouseImagePos[0],
                        mouseY = mouseImagePos[1];

                int endX = Math.min(Math.max(0, mouseX), app.getImage().getWidth() - 1),
                        endY = Math.min(Math.max(0, mouseY), app.getImage().getHeight() - 1);

                int margin = tool.getMargin();

                int startX = tool.getStartX(),
                        startY = tool.getStartY();

                int x = Math.min(startX, endX) - margin,
                        y = Math.min(startY, endY) - margin;
                int width = Math.abs(startX - endX) + 1 + 2 * margin,
                        height = Math.abs(startY - endY) + 1 + 2 * margin;

                tool.setX(x);
                tool.setY(y);
                tool.setWidth(width);
                tool.setHeight(height);
            }
            case DragTool.IDLE_DRAG -> {
                SVector delta = app.getMouseImagePosVec().copy().sub(dragStartMouseCoords);
                int x = dragStartX + (int) Math.round(delta.x);
                int y = dragStartY + (int) Math.round(delta.y);
                tool.setX(x);
                tool.setY(y);
            }
        }

        double zoom = app.getImageZoom();
        setFixedSize(new SVector(tool.getWidth(), tool.getHeight()).scale(zoom));

        SVector pos = new SVector(tool.getX(), tool.getY()).scale(zoom).add(app.getCanvas().getImageTranslation());
        clearAttachPoints();
        addAnchor(Anchor.TOP_LEFT, pos);
    }

    @Override
    public void mouseReleased(int mouseButton, int mods) {
        super.mouseReleased(mouseButton, mods);

        switch (mouseButton) {
            case GLFW.GLFW_MOUSE_BUTTON_LEFT -> {
                switch (tool.getState()) {
                    case DragTool.INITIAL_DRAG -> {
                        if (tool.enterIdle()) {
                            tool.start();
                            tool.setState(DragTool.IDLE);
                        } else {
                            tool.finish();
                        }
                    }
                    case DragTool.IDLE_DRAG -> {
                        tool.setState(DragTool.IDLE);
                    }
                }
            }
        }
    }

    private void startIdleDrag() {
        if (canStartIdleDrag()) {
            dragStartMouseCoords = app.getMouseImagePosVec();
            dragStartX = tool.getX();
            dragStartY = tool.getY();

            tool.setState(DragTool.IDLE_DRAG);
        }
    }

    protected abstract boolean canStartIdleDrag();

    @Override
    protected int getVisibleStates() {
        return DragTool.INITIAL_DRAG | DragTool.IDLE | DragTool.IDLE_DRAG;
    }
}