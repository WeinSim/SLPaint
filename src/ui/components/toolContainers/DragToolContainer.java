package ui.components.toolContainers;

import org.lwjgl.glfw.GLFW;

import main.apps.MainApp;
import main.tools.DragTool;
import main.tools.ImageTool;
import sutil.math.SVector;
import sutil.ui.UI;
import sutil.ui.UIColors;
import sutil.ui.UISizes;
import ui.components.SizeKnob;

public abstract sealed class DragToolContainer<T extends DragTool> extends ToolContainer<T>
        permits SelectionToolContainer, TextToolContainer {

    // IDLE_DRAG
    private SVector dragStartMouseCoords;
    private int dragStartX, dragStartY;

    // RESIZING
    private SizeKnob dragKnob;
    private SVector dragStartPos;
    private SVector dragStartSize;
    private SVector dragStartMouse;

    public DragToolContainer(T tool, MainApp app) {
        super(tool, app);

        clipToRoot = false;
        relativeLayer = 2;

        style.setStrokeCheckerboard(UIColors.SELECTION_BORDER_1, UIColors.SELECTION_BORDER_2, UISizes.CHECKERBOARD);
        style.setStrokeWeight(2.0);

        setMarginScale(1.0);

        for (int dy = 0; dy <= 2; dy++) {
            for (int dx = 0; dx <= 2; dx++) {
                if (dx == 1 && dy == 1)
                    continue;

                add(new SizeKnob(this, tool, dx, dy));
            }
        }

        setLeftClickAction(this::startIdleDrag);

        dragKnob = null;
        dragStartPos = new SVector();
        dragStartSize = new SVector();
        dragStartMouse = new SVector();
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
            case DragTool.RESIZING -> {
                if (!UI.isLeftMousePressed()) {
                    dragKnob = null;
                    tool.setState(DragTool.IDLE);
                    break;
                }
                if (dragKnob != null) {
                    SVector mouseDelta = app.getMouseImagePosVec().sub(dragStartMouse);
                    boolean lockRatio = tool == ImageTool.SELECTION ? MainApp.isLockSelectionRatio() : false;
                    dragKnob.updateSelection(dragStartPos, dragStartSize, mouseDelta, lockRatio);
                }
            }
        }

        double zoom = app.getImageZoom();
        setFixedSize(new SVector(tool.getWidth(), tool.getHeight()).scale(zoom));

        SVector pos = new SVector(tool.getX(), tool.getY()).scale(zoom).add(app.getCanvas().getImageTranslation());
        clearAnchors();
        addAnchor(Anchor.TOP_LEFT, pos);
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
        if (canStartIdleDrag()) {
            dragStartMouseCoords = app.getMouseImagePosVec();
            dragStartX = tool.getX();
            dragStartY = tool.getY();

            tool.setState(DragTool.IDLE_DRAG);
        }
    }

    protected abstract boolean canStartIdleDrag();

    public void startSizeDrag(SizeKnob knob) {
        dragKnob = knob;
        dragStartPos.set(tool.getX(), tool.getY());
        dragStartSize.set(tool.getWidth(), tool.getHeight());
        dragStartMouse.set(app.getMouseImagePosVec());

        tool.setState(DragTool.RESIZING);
    }

    @Override
    protected int getVisibleStates() {
        return DragTool.INITIAL_DRAG | DragTool.IDLE | DragTool.IDLE_DRAG | DragTool.RESIZING;
    }
}