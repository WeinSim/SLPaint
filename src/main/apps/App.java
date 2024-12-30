package main.apps;

import java.util.HashMap;
import java.util.LinkedList;

import org.lwjgl.glfw.GLFW;

import main.MainLoop;
import renderEngine.AppRenderer;
import renderEngine.Loader;
import renderEngine.Window;
import sutil.math.SVector;
import sutil.ui.UIAction;
import ui.AppUI;
import ui.ColorEditorUI;
import ui.MainUI;
import ui.SettingsUI;

public sealed abstract class App permits MainApp, ColorEditorApp, SettingsApp {

    public static final SVector[] DEFAULT_UI_COLORS = {
            new SVector(0.3, 0.3, 0.3),
            new SVector(0.07, 0.35, 0.5),
            new SVector(0.5, 0.35, 0.07),
            new SVector(0.5, 0.07, 0.35),
    };

    private static final double BACKGROUND_NORMAL_COLOR_SCALE = 0.24,
            BACKGROUND_HIGHLIGHT_COLOR_SCALE = 0.5,
            BACKGROUND_HIGHLIGHT_COLOR_2_SCALE = 0.7,
            OUTLINE_NORMAL_COLOR_SCALE = 1.2,
            OUTLINE_HIGHLIGHT_COLOR_SCALE = 1.2,
            SEPARATOR_COLOR_SCALE = 0.56;

    private static final double FRAME_TIME_GAMMA = 0.05;

    private static SVector baseColor = new SVector(DEFAULT_UI_COLORS[0]);

    protected Window window;

    protected double avgFrameTime = -1;

    private LinkedList<UIAction> eventQueue;

    protected AppUI<?> ui;
    private static boolean showDebugOutline = false;

    protected AppRenderer<?> renderer;
    private Loader loader;

    /**
     * Only used if this app is the child app of another app.
     */
    private App parent;
    /**
     * The type of dialog in the parent app that this app represents.
     */
    private int dialogType;

    private HashMap<Integer, App> childApps;

    public App(int width, int height, int windowMode, String title) {
        this(width, height, windowMode, true, title, null);
    }

    public App(int width, int height, int windowMode, boolean resizable, String title, App parent) {
        this.parent = parent;

        MainLoop.addApp(this);

        window = new Window(width, height, windowMode, resizable, title);
        childApps = new HashMap<>();
        loader = new Loader();
        eventQueue = new LinkedList<>();

        // init();

        // if (ui == null) {
        // createUI();
        // }
    }

    protected void createUI() {
        Class<? extends AppUI<?>> uiClass = switch (this) {
            case MainApp _ -> MainUI.class;
            case ColorEditorApp _ -> ColorEditorUI.class;
            case SettingsApp _ -> SettingsUI.class;
        };
        try {
            ui = uiClass.getDeclaredConstructor(getClass()).newInstance(this);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    // protected abstract void init();

    public void update(double deltaT) {
        if (avgFrameTime < 0) {
            avgFrameTime = deltaT;
        } else {
            avgFrameTime = (1 - FRAME_TIME_GAMMA) * avgFrameTime + FRAME_TIME_GAMMA * deltaT;
        }

        // empty event queue
        while (!eventQueue.isEmpty()) {
            eventQueue.removeFirst().run();
        }

        boolean[] keys = window.getKeys();
        boolean[] prevKeys = window.getPrevKeys();
        SVector mousePos = window.getMousePosition();
        boolean[] mouseButtons = window.getMouseButtons();
        boolean[] prevMouseButtons = window.getPrevMouseButtons();

        // toggle debug outline
        if (keyPressed(keys, prevKeys, GLFW.GLFW_KEY_COMMA)) {
            toggleDebugOutline();
        }

        // reload shaders
        if (keyPressed(keys, prevKeys, GLFW.GLFW_KEY_S) && keys[GLFW.GLFW_KEY_LEFT_SHIFT]) {
            renderer.reloadShaders();
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

    public void finish() {
        if (parent != null) {
            parent.clearChildApp(dialogType);
        }
    }

    public void showDialog(int dialogType) {
        App child = childApps.get(dialogType);
        if (child != null) {
            child.getWindow().requestFocus();
        } else {
            child = createChildApp(dialogType);
            child.setDialogType(dialogType);
            childApps.put(dialogType, child);
            GLFW.glfwMakeContextCurrent(window.getWindowHandle());
        }
    }

    protected abstract App createChildApp(int dialogType);

    public void clearChildApp(int dialogType) {
        childApps.remove(dialogType);
    }

    public final void render() {
        if (renderer == null) {
            renderer = new AppRenderer<App>(this);
        }
        renderer.render();
    }

    protected boolean keyPressed(boolean[] keys, boolean[] prevKeys, int key) {
        return keys[key] && !prevKeys[key];
    }

    public void queueEvent(UIAction action) {
        eventQueue.add(action);
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

    public double getFrameRate() {
        return 1.0 / avgFrameTime;
    }

    public static void setBaseColor(SVector baseColor) {
        App.baseColor.set(baseColor);
    }

    public static SVector getBaseColor() {
        return baseColor;
    }

    public static SVector getBackgroundNormalColor() {
        return new SVector(baseColor).scale(BACKGROUND_NORMAL_COLOR_SCALE);
    }

    public static SVector getBackgroundHighlightColor() {
        return new SVector(baseColor).scale(BACKGROUND_HIGHLIGHT_COLOR_SCALE);
    }

    public static SVector getBackgroundHighlightColor2() {
        return new SVector(baseColor).scale(BACKGROUND_HIGHLIGHT_COLOR_2_SCALE);
    }

    public static SVector getOutlineNormalColor() {
        return new SVector(baseColor).scale(OUTLINE_NORMAL_COLOR_SCALE);
    }

    public static SVector getOutlineHighlightColor() {
        return new SVector(baseColor).scale(OUTLINE_HIGHLIGHT_COLOR_SCALE);
    }

    public static SVector getSeparatorColor() {
        return new SVector(baseColor).scale(SEPARATOR_COLOR_SCALE);
    }

    public void setDialogType(int dialogType) {
        this.dialogType = dialogType;
    }
}