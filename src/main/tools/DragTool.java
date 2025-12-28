package main.tools;

import org.lwjgl.glfw.GLFW;

public abstract sealed class DragTool extends ImageTool permits SelectionTool, TextTool {

    // INITIAL_DRAG
    protected int startX, startY;
    protected int endX, endY;
    // IDLE, IDLE_DRAG
    protected int x, y;
    protected int width, height;

    @Override
    public boolean startInitialDrag(int x, int y, int mouseButton) {
        if (mouseButton != GLFW.GLFW_MOUSE_BUTTON_LEFT)
            return false;

        startX = Math.min(Math.max(0, x), app.getImage().getWidth() - 1);
        startY = Math.min(Math.max(0, y), app.getImage().getHeight() - 1);

        return true;
    }

    @Override
    protected void handleInitialDrag(int x, int y, int px, int py) {
        endX = Math.min(Math.max(0, x), app.getImage().getWidth() - 1);
        endY = Math.min(Math.max(0, y), app.getImage().getHeight() - 1);
    }

    @Override
    protected boolean finishInitialDrag() {
        int margin = getMargin();

        x = Math.min(startX, endX) + margin;
        y = Math.min(startY, endY) + margin;
        width = Math.abs(startX - endX) + 1 - 2 * margin;
        height = Math.abs(startY - endY) + 1 - 2 * margin;

        if (width == 1 || height == 1)
            return false;

        return true;
    }

    protected abstract int getMargin();

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return switch (getState()) {
            case INITIAL_DRAG -> Math.abs(startX - endX) + 1;
            default -> width;
        };
    }

    public int getHeight() {
        return switch (getState()) {
            case INITIAL_DRAG -> Math.abs(startY - endY) + 1;
            default -> height;
        };
    }

    public int getStartX() {
        return startX;
    }

    public int getStartY() {
        return startY;
    }

    public int getEndX() {
        return endX;
    }

    public int getEndY() {
        return endY;
    }
}