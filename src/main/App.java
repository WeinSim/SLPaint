package main;

import org.lwjgl.glfw.GLFW;

import renderEngine.AppRenderer;
import renderEngine.Loader;
import renderEngine.Window;
import sutil.math.SVector;
import ui.AppUI;

public abstract class App {

    private static boolean showDebugOutline = false;

    protected Window window;

    protected AppUI<?> ui;

    protected AppRenderer<?> renderer;
    private Loader loader;

    public App(int width, int height, int windowMode, String title) {
        this(width, height, windowMode, true, title);
    }

    public App(int width, int height, int windowMode, boolean resizable, String title) {
        window = new Window(width, height, windowMode, resizable, title);

        MainLoop.addApp(this);
    }

    public void init() {
        loader = new Loader();
    }

    public void update(double deltaT) {
        boolean[] keys = window.getKeys();
        boolean[] prevKeys = window.getPrevKeys();
        SVector mousePos = window.getMousePosition();
        boolean[] mouseButtons = window.getMouseButtons();
        boolean[] prevMouseButtons = window.getPrevMouseButtons();

        // toggle debug outline
        if (keyPressed(keys, prevKeys, GLFW.GLFW_KEY_COMMA)) {
            App.toggleDebugOutline();
        }

        // UI update
        int[] displaySize = window.getDisplaySize();
        // adding 1 to the width ensures that the "topRow"'s right edge isn't visible on
        // screen
        ui.setRootSize(displaySize[0] + 1, displaySize[1]);
        boolean focus = window.isFocused();
        // mouse should not be above anything when the window isn't focused
        if (!focus) {
            mousePos.set(-10000, -10000);
        }
        ui.update(mousePos);

        if (focus) {
            // UI mouse pressed
            if (mouseButtons[0] && !prevMouseButtons[0]) {
                ui.mousePressed(mousePos);
            }

            boolean shift = keys[GLFW.GLFW_KEY_LEFT_SHIFT] || keys[GLFW.GLFW_KEY_RIGHT_SHIFT];
            for (char c : window.getCharBuffer().toCharArray()) {
                ui.keyPressed(c, shift);
            }
            int[] specialKeys = {
                    GLFW.GLFW_KEY_BACKSPACE,
                    GLFW.GLFW_KEY_ENTER,
                    GLFW.GLFW_KEY_TAB
            };
            for (int i = 0; i < specialKeys.length; i++) {
                if (keyPressed(keys, prevKeys, specialKeys[i])) {
                    // TODO this is just a hack for now.
                    // GLFW offers key callbacks and char callbacks. Properly combining both would
                    // make things quite complicated.
                    ui.keyPressed((char) specialKeys[i], shift);
                }
            }
        }

        window.setArrowCursor();
        if (ui.mouseAboveTextInput()) {
            window.setIBeamCursor();
        }
    }

    public abstract void finish();

    public final void render() {
        if (renderer == null) {
            renderer = new AppRenderer<App>(this);
        }
        renderer.render();
    }

    protected boolean keyPressed(boolean[] keys, boolean[] prevKeys, int key) {
        return keys[key] && !prevKeys[key];
    }

    public Window getWindow() {
        return window;
    }

    public Loader getLoader() {
        return loader;
    }

    public AppUI<?> getUI() {
        return ui;
    }

    public static void toggleDebugOutline() {
        showDebugOutline = !showDebugOutline;
    }

    public static boolean showDebugOutline() {
        return showDebugOutline;
    }
}