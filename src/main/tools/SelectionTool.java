package main.tools;

import java.awt.image.BufferedImage;

import main.ClipboardManager;
import main.Image;
import main.apps.MainApp;
import sutil.math.SVector;

public final class SelectionTool extends DragTool {

    public static final SelectionTool INSTANCE = new SelectionTool();

    private Image selection;

    private SelectionTool() {
        selection = null;
    }

    @Override
    public int getMargin() {
        return 0;
    }

    @Override
    public boolean enterIdle() {
        return width > 1 && height > 1;
    }

    @Override
    public void init() {
        createSubImage();
    }

    @Override
    public void finish() {
        // can selection ever be null here?
        if (selection != null) {
            app.getImage().drawSubImage(x, y, selection.getBufferedImage());
            clearSelection();
        }
    }

    private void createSubImage() {
        selection = new Image(app.getImage().getSubImage(x, y, width, height,
                MainApp.isTransparentSelection() ? app.getSecondaryColor() : null));
        app.getImage().setPixels(x, y, width, height, app.getSecondaryColor());
    }

    public void clearSelection() {
        if (selection != null) {
            selection.cleanUp();
        }
        selection = null;
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
    }

    public void moveSelection(int dx, int dy) {
        x += dx;
        y += dy;
    }

    public Image getSelection() {
        return selection;
    }

    @Override
    public String getName() {
        return "Selection";
    }
}