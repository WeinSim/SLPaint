package main.tools;

import main.Image;

public final class PencilTool extends ImageTool {

    public static final PencilTool INSTANCE = new PencilTool();

    public static final int MIN_SIZE = 1, MAX_SIZE = 16;

    private int size = 1;

    private PencilTool() {
    }

    @Override
    public void click(int x, int y, int mouseButton) {
        Image image = app.getImage();
        return x >= 0 && x < image.getWidth() && y >= 0 && y < image.getHeight();
    }

    @Override
    protected void handleInitialDrag(int x, int y, int px, int py) {
        app.drawLine(x, y, px, py, size, getMouseDragButton() == 0 ? app.getPrimaryColor() : app.getSecondaryColor());
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = Math.min(Math.max(size, MIN_SIZE), MAX_SIZE);
    }

    @Override
    public String getName() {
        return "Pencil";
    }
}