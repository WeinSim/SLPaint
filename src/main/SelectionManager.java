package main;

import java.awt.image.BufferedImage;

import main.apps.MainApp;
import sutil.math.SVector;

public class SelectionManager {

    public static final int NONE = 0, CREATING = 1, IDLE = 2, DRAGGING = 3, RESIZING = 4;

    private MainApp app;

    private int phase;
    private Image selection;
    // CREATING_SELECTION
    private int startX, startY;
    private int endX, endY;
    // IDLE_SELECTION, DRAGGING_SELECTION
    private int x, y;
    private int width, height;
    // DRAGGING_SELECTION
    private int dragStartX, dragStartY; // where the image started out before it was dragged
    private SVector dragStartMouseCoords; // where the mouse was when it started dragging (world coords)

    public SelectionManager(MainApp app) {
        this.app = app;

        phase = NONE;
        selection = null;
    }

    public void update() {
        SVector mousePos = app.getMouseImagePosVec();
        int mouseX = (int) Math.round(mousePos.x),
                mouseY = (int) Math.round(mousePos.y);
        switch (phase) {
            case NONE, IDLE -> {
            }
            case CREATING -> {
                endX = Math.min(Math.max(0, mouseX), app.getImage().getWidth());
                endY = Math.min(Math.max(0, mouseY), app.getImage().getHeight());
            }
            case DRAGGING -> {
                SVector delta = mousePos.copy().sub(dragStartMouseCoords);
                x = dragStartX + (int) Math.round(delta.x);
                y = dragStartY + (int) Math.round(delta.y);
            }
            case RESIZING -> {
                // TODO
            }
        }
    }

    public void startCreating() {
        if (phase != NONE) {
            return;
        }

        SVector mousePos = app.getMouseImagePosVec();
        int mouseX = (int) Math.round(mousePos.x),
                mouseY = (int) Math.round(mousePos.y);
        startX = Math.min(Math.max(0, mouseX), app.getImage().getWidth());
        startY = Math.min(Math.max(0, mouseY), app.getImage().getHeight());

        phase = CREATING;
    }

    public void finishCreating() {
        if (phase != CREATING) {
            return;
        }

        x = Math.min(startX, endX);
        y = Math.min(startY, endY);
        width = Math.abs(startX - endX);
        height = Math.abs(startY - endY);

        if (width == 0 || height == 0) {
            cancel();
            return;
        }

        createSubImage();

        phase = IDLE;
    }

    private void createSubImage() {
        selection = new Image(app.getImage().getSubImage(x, y, width, height,
                app.isTransparentSelection() ? app.getSecondaryColor() : null));
        app.getImage().setPixels(x, y, width, height, app.getSecondaryColor());
    }

    public void selectEverything() {
        // if (phase != NONE) {
        // return;
        // }
        cancel();
        x = 0;
        y = 0;
        width = app.getImage().getWidth();
        height = app.getImage().getHeight();
        createSubImage();

        phase = IDLE;
    }

    public void selectClipboard(BufferedImage image) {
        selectClipboard(new Image(image));
    }

    public void selectClipboard(Image image) {
        cancel();

        x = 0;
        y = 0;
        width = image.getWidth();
        height = image.getHeight();

        selection = image;

        phase = IDLE;
    }

    public void startDragging() {
        if (phase != IDLE) {
            return;
        }
        dragStartMouseCoords = app.getMouseImagePosVec();
        dragStartX = x;
        dragStartY = y;

        phase = DRAGGING;
    }

    public void finishDragging() {
        if (phase != DRAGGING) {
            return;
        }

        phase = IDLE;
    }

    public void startResizing() {
        if (phase != IDLE) {
            return;
        }
        throw new UnsupportedOperationException("Selection resizing is not implemented yet");
    }

    public void finishResizing() {
        if (phase != RESIZING) {
            return;
        }
        throw new UnsupportedOperationException("Selection resizing is not implemented yet");
    }

    public void finish() {
        app.getImage().setSubImage(selection.getBufferedImage(), x, y);
        cancel();
    }

    public void cancel() {
        if (selection != null) {
            selection.cleanUp();
        }
        selection = null;

        phase = NONE;
    }

    public boolean mouseAboveSelection(int mouseX, int mouseY) {
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
        return switch (phase) {
            case CREATING -> Math.abs(startX - endX);
            default -> width;
        };
    }

    public int getHeight() {
        return switch (phase) {
            case CREATING -> Math.abs(startY - endY);
            default -> height;
        };
    }

    public int getPhase() {
        return phase;
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

    // public void startSelection() {
    // if (selectionPhase != NO_SELECTION) {
    // return;
    // }
    // selectionPhase = CREATING_SELECTION;
    // int[] mousePos = app.getMouseImagePosition();
    // selectionStartX = mousePos[0];
    // selectionStartY = mousePos[1];
    // }

    // public void cancelSelection() {
    // selectionPhase = NO_SELECTION;
    // selection = null;
    // }

    // public void endSelection() {
    // Image image = app.getImage();
    // image.setSubImage(selection.getBufferedImage(), selectionPosX,
    // selectionPosY);
    // selectionPhase = NO_SELECTION;
    // selection = null;
    // }
}