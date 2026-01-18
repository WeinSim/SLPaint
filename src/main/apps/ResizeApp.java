package main.apps;

import org.lwjgl.glfw.GLFW;

import renderEngine.Window;
import sutil.ui.UI;
import sutil.ui.UINumberInput;
import ui.AppUI;
import ui.ResizeUI;

public final class ResizeApp extends App {

    public static final int PIXELS = 0, PERCENTAGE = 1;

    private MainApp mainApp;

    private UINumberInput widthInput;

    private int widthPixels = 0, heightPixels = 0;
    private double widthPercentage = 0, heightPercentage = 0;

    private final int initialWidth, initialHeight;

    public ResizeApp(MainApp mainApp) {
        super(500, 500, Window.NORMAL, false, true, "Resize Image", mainApp);

        this.mainApp = mainApp;

        initialWidth = mainApp.getImage().getWidth();
        initialHeight = mainApp.getImage().getHeight();
        setWidthPixels(initialWidth);
        setHeightPixels(initialHeight);

        addKeyboardShortcut(GLFW.GLFW_KEY_CAPS_LOCK, 0, this::cancel, false);

        addKeyboardShortcut(GLFW.GLFW_KEY_ENTER, 0, this::done, true);
    }

    @Override
    public void update(double deltaT) {
        super.update(deltaT);

        if (frameCount == 1) {
            UI.select(widthInput);
        }
    }

    @Override
    protected App createChildApp(int dialogType) {
        return null;
    }

    @Override
    protected AppUI<?> createUI() {
        return new ResizeUI(this);
    }

    public void setWidthInput(UINumberInput widthInput) {
        this.widthInput = widthInput;
    }

    public void done() {
        mainApp.queueEvent(() -> mainApp.resizeImage(widthPixels, heightPixels, false));
        window.requestClose();
    }

    public void cancel() {
        window.requestClose();
    }

    public int getWidthPixels() {
        return widthPixels;
    }

    public void setWidthPixels(int widthPixels) {
        // this.widthPixels = Math.min(Math.max(MainApp.MIN_IMAGE_SIZE, widthPixels),
        // MainApp.MAX_IMAGE_SIZE);
        this.widthPixels = widthPixels;
        clampWidthPixels();

        widthPercentage = 100.0 * this.widthPixels / initialWidth;
    }

    public int getHeightPixels() {
        return heightPixels;
    }

    public void setHeightPixels(int heightPixels) {
        // this.heightPixels = Math.min(Math.max(MainApp.MIN_IMAGE_SIZE, heightPixels),
        // MainApp.MAX_IMAGE_SIZE);
        this.heightPixels = heightPixels;
        clampHeightPixels();

        heightPercentage = 100.0 * this.heightPixels / initialHeight;
    }

    public int getWidthPercentage() {
        return (int) widthPercentage;
    }

    public void setWidthPercentage(int widthPercentage) {
        this.widthPercentage = widthPercentage;

        widthPixels = (int) (this.widthPercentage / 100.0 * initialWidth);
        clampWidthPixels();
    }

    public int getHeightPercentage() {
        return (int) heightPercentage;
    }

    public void setHeightPercentage(int heightPercentage) {
        this.heightPercentage = heightPercentage;

        heightPixels = (int) (this.heightPercentage / 100.0 * initialHeight);
        clampHeightPixels();
    }

    private void clampWidthPixels() {
        if (widthPixels < MainApp.MIN_IMAGE_SIZE) {
            setWidthPixels(MainApp.MIN_IMAGE_SIZE);
        } else if (widthPixels > MainApp.MAX_IMAGE_SIZE) {
            setWidthPixels(MainApp.MAX_IMAGE_SIZE);
        }
    }

    private void clampHeightPixels() {
        if (heightPixels < MainApp.MIN_IMAGE_SIZE) {
            setHeightPixels(MainApp.MIN_IMAGE_SIZE);
        } else if (heightPixels > MainApp.MAX_IMAGE_SIZE) {
            setHeightPixels(MainApp.MAX_IMAGE_SIZE);
        }
    }
}