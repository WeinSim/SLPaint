package main.tools;

import main.Image;

public final class PencilTool extends ImageTool {

    public static final PencilTool INSTANCE = new PencilTool();

    private PencilTool() {
    }

    @Override
    protected boolean startInitialDrag(int x, int y, int mouseButton) {
        Image image = app.getImage();
        return x >= 0 && x < image.getWidth() && y >= 0 && y < image.getHeight();
    }

    @Override
    protected void handleInitialDrag(int x, int y, int px, int py) {
        app.drawLine(x, y, px, py, getMouseDragButton() == 0 ? app.getPrimaryColor() : app.getSecondaryColor());
    }

    @Override
    protected boolean finishInitialDrag() {
        return false;
    }

    @Override
    protected boolean startIdleDrag(int x, int y, int mouseButton) {
        invalidState();
        return false;
    }

    @Override
    protected void handleIdleDrag(int x, int y, int px, int py) {
        invalidState();
    }

    @Override
    protected void finishIdleDrag() {
        invalidState();
    }

    @Override
    public void forceQuit() {
        // nothing to do
    }

    private void invalidState() {
        throw new UnsupportedOperationException("IDLE and IDLE_DRAG states undefined for Pencil tool");
    }

    @Override
    public String getName() {
        return "Pencil";
    }
}