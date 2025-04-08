package main.tools;

import java.awt.image.BufferedImage;

import org.lwjgl.glfw.GLFW;

import main.ClipboardManager;
import main.Image;
import main.apps.MainApp;
import sutil.math.SVector;

public final class SelectionTool extends ImageTool {

    public static final SelectionTool INSTANCE = new SelectionTool();

    private Image selection;
    // INITIAL_DRAG
    private int startX, startY;
    private int endX, endY;
    // IDLE, IDLE_DRAG
    private int x, y;
    private int width, height;
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
    public void start() {
        super.start();

        selection = null;
    }

    @Override
    public boolean startInitialDrag(int x, int y, int mouseButton) {
        if (mouseButton != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return false;
        }

        startX = Math.min(Math.max(0, x), app.getImage().getWidth());
        startY = Math.min(Math.max(0, y), app.getImage().getHeight());

        return true;
    }

    @Override
    protected void handleInitialDrag(int x, int y, int px, int py) {
        endX = Math.min(Math.max(0, x), app.getImage().getWidth());
        endY = Math.min(Math.max(0, y), app.getImage().getHeight());
    }

    @Override
    protected boolean finishInitialDrag() {
        x = Math.min(startX, endX);
        y = Math.min(startY, endY);
        width = Math.abs(startX - endX);
        height = Math.abs(startY - endY);

        if (width == 0 || height == 0) {
            return false;
        }

        createSubImage();

        return true;
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
    }

    private void createSubImage() {
        selection = new Image(app.getImage().getSubImage(x, y, width, height,
                MainApp.isTransparentSelection() ? app.getSecondaryColor() : null));
        app.getImage().setPixels(x, y, width, height, app.getSecondaryColor());
    }

    private void flattenSelection() {
        app.getImage().setSubImage(selection.getBufferedImage(), this.x, this.y);
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
        if (paste == null) {
            return;
        }

        x = 0;
        y = 0;
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

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return switch (getState()) {
            case INITIAL_DRAG -> Math.abs(startX - endX);
            default -> width;
        };
    }

    public int getHeight() {
        return switch (getState()) {
            case INITIAL_DRAG -> Math.abs(startY - endY);
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

    @Override
    public String getName() {
        return "Selection";
    }
}