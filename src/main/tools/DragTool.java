package main.tools;

public abstract sealed class DragTool extends ImageTool permits SelectionTool, TextTool {

    protected int x, y;
    protected int width, height;

    public DragTool() {
        x = 0;
        y = 0;
        width = 0;
        height = 0;
    }

    @Override
    public void click(int x, int y, int mouseButton) {
        // TODO: have the startInitialDrag start here?
    }

    /**
     * 
     * @return Wether the {@code IDLE} state should be entered after the initial
     *         drag
     */
    public abstract boolean enterIdle();

    public abstract void init();

    public abstract int getMargin();

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}