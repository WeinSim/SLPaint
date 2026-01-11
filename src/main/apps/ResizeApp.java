package main.apps;

import org.lwjgl.glfw.GLFW;

import renderEngine.Window;
import ui.AppUI;
import ui.ResizeUI;

public final class ResizeApp extends App {

    private MainApp mainApp;

    private int newImageWidth = 0,
            newImageHeight = 0;

    public ResizeApp(MainApp mainApp) {
        super(500, 500, Window.NORMAL, false, true, "Resize Image", mainApp);

        this.mainApp = mainApp;

        newImageWidth = mainApp.getImage().getWidth();
        newImageHeight = mainApp.getImage().getHeight();

        addKeyboardShortcut(GLFW.GLFW_KEY_CAPS_LOCK, 0, window::requestClose, false);

        addKeyboardShortcut(GLFW.GLFW_KEY_ENTER, 0, this::done, true);
    }

    @Override
    protected App createChildApp(int dialogType) {
        return null;
    }

    @Override
    protected AppUI<?> createUI() {
        return new ResizeUI(this);
    }

    public void done() {
        mainApp.getImage().changeSize(newImageWidth, newImageHeight, mainApp.getSecondaryColor());
        window.requestClose();
    }

    public int getNewImageWidth() {
        return newImageWidth;
    }

    public void setNewImageWidth(int newImageWidth) {
        this.newImageWidth = Math.min(Math.max(MainApp.MIN_IMAGE_SIZE, newImageWidth), MainApp.MAX_IMAGE_SIZE);
    }

    public int getNewImageHeight() {
        return newImageHeight;
    }

    public void setNewImageHeight(int newImageHeight) {
        this.newImageHeight = Math.min(Math.max(MainApp.MIN_IMAGE_SIZE, newImageHeight), MainApp.MAX_IMAGE_SIZE);
    }
}