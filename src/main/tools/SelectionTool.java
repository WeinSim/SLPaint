package main.tools;

import java.awt.image.BufferedImage;

import org.lwjgl.glfw.GLFW;

import main.ClipboardManager;
import main.Image;
import main.apps.MainApp;
import sutil.math.SVector;

public final class SelectionTool extends DragTool {

    public static final SelectionTool INSTANCE = new SelectionTool();

    private Image selection;

    // IDLE_DRAG
    private int dragStartX, dragStartY; // where the image started out before it was dragged
    private SVector dragStartMouseCoords; // where the mouse was when it started dragging (world coords)

    private SelectionTool() {
        selection = null;

        // Ctrl + A: select everything
        addKeyboardShortcut(new KeyboardShortcut(
                GLFW.GLFW_KEY_A, GLFW.GLFW_MOD_CONTROL, NONE | IDLE, IDLE, this::selectEverything));

        // Esc: finish selection
        addKeyboardShortcut(new KeyboardShortcut(
                GLFW.GLFW_KEY_CAPS_LOCK, 0, IDLE, NONE, this::flattenSelection));

        // Del: delete selection
        addKeyboardShortcut(new KeyboardShortcut(
                GLFW.GLFW_KEY_DELETE, 0, IDLE, NONE, this::clearSelection));

        // Ctrl + V: paste
        addKeyboardShortcut(new KeyboardShortcut(
                GLFW.GLFW_KEY_V, GLFW.GLFW_MOD_CONTROL, NONE | IDLE, IDLE, this::pasteFromClipboard));

        // Ctrl + C: copy
        addKeyboardShortcut(new KeyboardShortcut(
                GLFW.GLFW_KEY_C, GLFW.GLFW_MOD_CONTROL, IDLE, IDLE, this::copyToClipboard));

        // Ctrl + X: cut
        addKeyboardShortcut(new KeyboardShortcut(
                GLFW.GLFW_KEY_X, GLFW.GLFW_MOD_CONTROL, IDLE, NONE, this::cutToClipboard));
    }

    @Override
    protected int getMargin() {
        return 0;
    }

    @Override
    protected boolean finishInitialDrag() {
        if (super.finishInitialDrag()) {
            createSubImage();
            return true;
        }

        return false;
    }

    @Override
    protected boolean startIdleDrag(int x, int y, int mouseButton) {
        if (mouseAboveSelection(x, y)) {
            dragStartMouseCoords = app.getMouseImagePosVec();
            dragStartX = this.x;
            dragStartY = this.y;
            return true;
        } else {
            flattenSelection();
            return false;
        }
    }

    @Override
    protected void handleIdleDrag(int x, int y, int px, int py) {
        SVector delta = app.getMouseImagePosVec().copy().sub(dragStartMouseCoords);
        this.x = dragStartX + (int) Math.round(delta.x);
        this.y = dragStartY + (int) Math.round(delta.y);
    }

    @Override
    protected void finishIdleDrag() {
        // nothing to do here
    }

    @Override
    public void forceQuit() {
        if (selection != null) {
            flattenSelection();
        }

        super.forceQuit();
    }

    private void createSubImage() {
        selection = new Image(app.getImage().getSubImage(x, y, width, height,
                MainApp.isTransparentSelection() ? app.getSecondaryColor() : null));
        app.getImage().setPixels(x, y, width, height, app.getSecondaryColor());
    }

    private void flattenSelection() {
        app.getImage().drawSubImage(x, y, selection.getBufferedImage());
        clearSelection();
    }

    private void clearSelection() {
        if (selection != null) {
            selection.cleanUp();
        }
        selection = null;
    }

    private void selectEverything() {
        if (selection != null) {
            flattenSelection();
        }

        x = 0;
        y = 0;
        width = app.getImage().getWidth();
        height = app.getImage().getHeight();
        createSubImage();
    }

    private void copyToClipboard() {
        ClipboardManager.setImage(selection.getBufferedImage());
    }

    private void cutToClipboard() {
        copyToClipboard();
        clearSelection();
    }

    private void pasteFromClipboard() {
        BufferedImage paste = ClipboardManager.getImage();
        if (paste == null)
            return;

        if (selection != null) {
            flattenSelection();
        }

        SVector spawnPos = app.getImagePosition(app.getCanvas().getAbsolutePosition());
        x = Math.min(Math.max((int) spawnPos.x, 0), app.getImage().getWidth() - paste.getWidth());
        y = Math.min(Math.max((int) spawnPos.y, 0), app.getImage().getHeight() - paste.getHeight());
        width = paste.getWidth();
        height = paste.getHeight();

        selection = new Image(paste);
    }

    private boolean mouseAboveSelection(int mouseX, int mouseY) {
        return mouseX >= x && mouseX < x + width
                && mouseY >= y && mouseY < y + height;
    }

    public Image getSelection() {
        return selection;
    }

    @Override
    public String getName() {
        return "Selection";
    }
}