package ui.components.toolContainers;

import java.util.function.Supplier;

import org.lwjgl.glfw.GLFW;

import main.apps.MainApp;
import main.tools.DragTool;
import sutil.math.SVector;
import sutil.ui.UIColors;
import sutil.ui.UISizes;
import ui.components.SizeKnob;

public abstract sealed class DragToolContainer<T extends DragTool> extends ToolContainer<T>
        permits SelectionToolContainer, TextToolContainer {

    // IDLE_DRAG
    private SVector dragStartMouseCoords;
    private int dragStartX, dragStartY;

    public DragToolContainer(T tool, MainApp app) {
        super(tool, app);

        relativeLayer = 2;

        style.setStrokeCheckerboard(UIColors.SELECTION_BORDER_1, UIColors.SELECTION_BORDER_2, UISizes.CHECKERBOARD);
        style.setStrokeWeight(2.0);

        addAnchor(Anchor.TOP_LEFT, this::getPos);

        final int visibleStates = DragTool.IDLE | DragTool.IDLE_DRAG;
        Supplier<Boolean> sizeKnobVis = () -> (tool.getState() & visibleStates) != 0;
        for (int dy = 0; dy <= 2; dy++) {
            for (int dx = 0; dx <= 2; dx++) {
                if (dx == 1 && dy == 1)
                    continue;

                add(new SizeKnob(dx, dy, this, tool, sizeKnobVis, app));
            }
        }

        setLeftClickAction(this::startIdleDrag);

        setCursorShape(
                () -> mouseAbove && ((tool.getState() & (DragTool.IDLE | DragTool.IDLE_DRAG)) != 0)
                        ? GLFW.GLFW_POINTING_HAND_CURSOR
                        : null);
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
    }

    @Override
    public void mouseReleased(int mouseButton, int mods) {
        super.mouseReleased(mouseButton, mods);

        switch (mouseButton) {
            case GLFW.GLFW_MOUSE_BUTTON_LEFT -> {
                switch (tool.getState()) {
                    case DragTool.INITIAL_DRAG -> {
                        if (tool.enterIdle())
                            tool.start();
                        else
                            tool.finish();
                    }
                    case DragTool.IDLE_DRAG -> {
                        tool.setState(DragTool.IDLE);
                    }
                }
            }
        }
    }

    private void startIdleDrag() {
        dragStartMouseCoords = app.getMouseImagePosVec();
        dragStartX = tool.getX();
        dragStartY = tool.getY();

        tool.setState(DragTool.IDLE_DRAG);
    }

    @Override
    protected int getVisibleStates() {
        return DragTool.INITIAL_DRAG | DragTool.IDLE | DragTool.IDLE_DRAG;
    }

    private SVector getPos() {
        double zoom = app.getImageZoom();
        return new SVector(tool.getX(), tool.getY()).scale(zoom).add(app.getCanvas().getImageTranslation());
    }
}