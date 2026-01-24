package main.tools;

import org.lwjgl.glfw.GLFW;

public abstract sealed class DragTool extends ImageTool implements Resizable permits SelectionTool, TextTool {

    public static final int INITIAL_DRAG = 0x02, IDLE = 0x04, IDLE_DRAG = 0x08;

    // INITIAL_DRAG
    private int startX, startY;

    protected int x, y;
    protected int width, height;

    public DragTool() {
        super();

        x = 0;
        y = 0;
        width = 0;
        height = 0;
    }

    @Override
    public void click(int x, int y, int mouseButton) {
        if (state == NONE) {
            if (mouseButton == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                startX = Math.min(Math.max(0, x), app.getImage().getWidth() - 1);
                startY = Math.min(Math.max(0, y), app.getImage().getHeight() - 1);

                state = INITIAL_DRAG;
            }
        } else {
            finish();
        }
    }

    /**
     * 
     * @return Wether the {@code IDLE} state should be entered after the initial
     *         drag
     */
    public abstract boolean enterIdle();

    public abstract void start();

    public abstract int getMargin();

    public int getState() {
        return state;
    }

    public int getStartX() {
        return startX;
    }

    public int getStartY() {
        return startY;
    }

    @Override
    public void startResizing() {
    }

    @Override
    public void finishResizing() {
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public void setX(int x) {
        this.x = x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public void setY(int y) {
        this.y = y;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public void setWidth(int width) {
        this.width = width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void setHeight(int height) {
        this.height = height;
    }

}