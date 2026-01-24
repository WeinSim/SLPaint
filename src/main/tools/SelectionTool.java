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

    private SelectionTool() {
        selection = null;

        // Ctrl + A: select everything
        addKeyboardShortcut(GLFW.GLFW_KEY_A, GLFW.GLFW_MOD_CONTROL, NONE | IDLE, this::selectEverything);

        // Esc: finish selection
        addKeyboardShortcut(GLFW.GLFW_KEY_CAPS_LOCK, 0, IDLE, this::finish);

        // Del: delete selection
        addKeyboardShortcut(GLFW.GLFW_KEY_DELETE, 0, IDLE, this::clearSelection);

        // Ctrl + V: paste
        addKeyboardShortcut(GLFW.GLFW_KEY_V, GLFW.GLFW_MOD_CONTROL, NONE | IDLE, this::pasteFromClipboard);

        // Ctrl + C: copy
        addKeyboardShortcut(GLFW.GLFW_KEY_C, GLFW.GLFW_MOD_CONTROL, IDLE, this::copyToClipboard);

        // Ctrl + X: cut
        addKeyboardShortcut(GLFW.GLFW_KEY_X, GLFW.GLFW_MOD_CONTROL, IDLE, this::cutToClipboard);

        // Ctrl + Shift + X: crop image to selection
        addKeyboardShortcut(GLFW.GLFW_KEY_X, GLFW.GLFW_MOD_CONTROL | GLFW.GLFW_MOD_SHIFT, IDLE,
                () -> {
                    finish();
                    app.cropImage(x, y, width, height);
                });

        // Arrow keys: move selection
        addKeyboardShortcut(GLFW.GLFW_KEY_UP, 0, IDLE, () -> y--);
        addKeyboardShortcut(GLFW.GLFW_KEY_DOWN, 0, IDLE, () -> y++);
        addKeyboardShortcut(GLFW.GLFW_KEY_LEFT, 0, IDLE, () -> x--);
        addKeyboardShortcut(GLFW.GLFW_KEY_RIGHT, 0, IDLE, () -> x++);
    }

    @Override
    public boolean enterIdle() {
        return width > 1 && height > 1;
    }

    @Override
    public void start() {
        createSubImage();
    }

    private void createSubImage() {
        selection = new Image(app.getImage().getSubImage(x, y, width, height,
                MainApp.isTransparentSelection() ? app.getSecondaryColor() : null));
        app.getImage().setPixels(x, y, width, height, app.getSecondaryColor());

        state = IDLE;
    }

    @Override
    public void finish() {
        if (selection != null) {
            app.renderImageToImage(selection, x, y, width, height);
            clearSelection();
        }

        state = NONE;
    }

    private void clearSelection() {
        if (selection != null) {
            selection.cleanUp();
        }
        selection = null;

        state = NONE;
    }

    public void selectEverything() {
        finish();

        x = 0;
        y = 0;
        width = app.getImage().getWidth();
        height = app.getImage().getHeight();
        createSubImage();
    }

    public void copyToClipboard() {
        ClipboardManager.setImage(selection.getBufferedImage());
    }

    public void cutToClipboard() {
        copyToClipboard();
        clearSelection();
    }

    public void pasteFromClipboard() {
        BufferedImage paste = ClipboardManager.getImage();
        if (paste == null)
            return;

        finish();

        SVector spawnPos = app.getImagePosition(app.getCanvas().getAbsolutePosition());
        x = Math.min(Math.max((int) spawnPos.x, 0), app.getImage().getWidth() - paste.getWidth());
        y = Math.min(Math.max((int) spawnPos.y, 0), app.getImage().getHeight() - paste.getHeight());
        width = paste.getWidth();
        height = paste.getHeight();

        selection = new Image(paste);

        state = IDLE;
    }

    @Override
    public int getMargin() {
        return 0;
    }

    @Override
    public String getName() {
        return "Selection";
    }

    public Image getSelection() {
        return selection;
    }

    public void moveSelection(int dx, int dy) {
        x += dx;
        y += dy;
    }

    @Override
    public boolean lockRatio() {
        return MainApp.isLockSelectionRatio();
    }
}