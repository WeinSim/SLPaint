package main.apps;

import java.util.HashMap;
import java.util.LinkedList;

import org.lwjgl.glfw.GLFW;

import main.MainLoop;
import main.settings.BooleanSetting;
import renderEngine.AppRenderer;
import renderEngine.Loader;
import renderEngine.Window;
import sutil.math.SVector;
import sutil.ui.UIAction;
import sutil.ui.UIRoot;
import ui.AppUI;
import ui.ColorEditorUI;
import ui.MainUI;
import ui.SettingsUI;

public sealed abstract class App permits MainApp, ColorEditorApp, SettingsApp {

    private static final double FRAME_TIME_GAMMA = 0.05;

    private static boolean showDebugOutline = false;
    private static BooleanSetting circularHueSatField = new BooleanSetting("hueSatCircle");
    private static BooleanSetting hslColorSpace = new BooleanSetting("hslColorSpace");

    protected Window window;

    protected boolean[] keys;
    protected boolean[] mouseButtons;
    protected SVector mousePos, prevMousePos;

    protected boolean closeOnEscape = true;

    protected double avgFrameTime = -1;
    protected int frameCount = 0;

    private LinkedList<UIAction> eventQueue;

    protected AppUI<?> ui;
    protected boolean adjustSizeOnInit = false;

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

        keys = new boolean[512];
        mouseButtons = new boolean[2];
        mousePos = new SVector();
        prevMousePos = null;
    }

    protected void createUI() {
        ui = switch (this) {
            case MainApp m -> new MainUI(m);
            case ColorEditorApp c -> new ColorEditorUI(c);
            case SettingsApp s -> new SettingsUI(s);
        };

        if (adjustSizeOnInit) {
            UIRoot root = ui.getRoot();
            SVector rootSize = root.getSize();
            int width = (int) rootSize.x;
            int height = (int) rootSize.y;
            window.setSizeAndCenter(width, height);
        }
    }

    public void update(double deltaT) {
        if (avgFrameTime < 0) {
            avgFrameTime = deltaT;
        } else {
            avgFrameTime = (1 - FRAME_TIME_GAMMA) * avgFrameTime + FRAME_TIME_GAMMA * deltaT;
        }
        frameCount++;

        if (prevMousePos == null) {
            mousePos = window.getMousePosition();
            prevMousePos = new SVector(mousePos);
        } else {
            prevMousePos.set(mousePos);
            mousePos = window.getMousePosition();
        }

        boolean focus = window.isFocused();

        // process char input
        Character c;
        while ((c = window.getNextCharacter()) != null) {
            if (focus) {
                ui.charInput(c);
            }
            charInput(c);
        }

        // process key input
        Window.KeyPressInfo keyPressInfo;
        while ((keyPressInfo = window.getNextKeyPressInfo()) != null) {
            int key = keyPressInfo.key();
            int mods = keyPressInfo.mods();
            switch (keyPressInfo.action()) {
                case GLFW.GLFW_PRESS, GLFW.GLFW_REPEAT -> {
                    if (key == GLFW.GLFW_KEY_CAPS_LOCK && closeOnEscape) {
                        window.requestClose();
                    }

                    if (0 <= key && key < keys.length) {
                        keys[key] = true;
                    }

                    if (focus) {
                        ui.keyPressed(key, mods);
                    }
                    keyPressed(key, mods);
                }
                case GLFW.GLFW_RELEASE -> {
                    if (0 <= key && key < keys.length) {
                        keys[key] = false;
                    }

                    keyReleased(key, mods);
                }
            }
        }

        // process mouse button input
        Window.MouseButtonInfo mouseButtonInfo;
        while ((mouseButtonInfo = window.getNextMouseButtonInfo()) != null) {
            int button = mouseButtonInfo.button();
            int mods = mouseButtonInfo.mods();
            switch (mouseButtonInfo.action()) {
                case GLFW.GLFW_PRESS -> {
                    if (0 <= button && button < mouseButtons.length) {
                        mouseButtons[button] = true;
                    }

                    if (focus) {
                        ui.mousePressed(button, mods);
                    }
                    mousePressed(button, mods);
                }
                case GLFW.GLFW_RELEASE -> {
                    if (0 <= button && button < mouseButtons.length) {
                        mouseButtons[button] = false;
                    }

                    if (focus) {
                        ui.mouseReleased(button, mods);
                    }
                    mouseReleased(button, mods);
                }
            }
        }

        // process scroll input
        Window.ScrollInfo scrollInfo;
        while ((scrollInfo = window.getNextScrollInfo()) != null) {
            if (focus) {
                ui.mouseWheel(new SVector(scrollInfo.xoffset(), scrollInfo.yoffset()), mousePos);
            }
            mouseScroll(scrollInfo.xoffset(), scrollInfo.yoffset());
        }

        // empty event queue
        while (!eventQueue.isEmpty()) {
            eventQueue.removeFirst().run();
        }

        int[] displaySize = window.getDisplaySize();
        ui.setRootSize(displaySize[0], displaySize[1]);

        SVector mousePos = window.getMousePosition();
        ui.update(mousePos, focus);

        if (ui.mouseAboveTextInput()) {
            window.setIBeamCursor();
        } else {
            window.setArrowCursor();
        }
    }

    protected void charInput(char c) {
    }

    protected void keyPressed(int key, int mods) {
        // System.out.format("Key pressed: key = %d, mods = %d, time = %d\n", key, mods,
        // System.nanoTime());

        switch (key) {
            // Comma -> toggle debug outline
            case GLFW.GLFW_KEY_COMMA -> toggleDebugOutline();

            // Shift + S -> reload shaders
            case GLFW.GLFW_KEY_S -> {
                if ((mods & GLFW.GLFW_MOD_SHIFT) != 0) {
                    renderer.reloadShaders();
                }
            }

            // Shift + R -> reload UI
            case GLFW.GLFW_KEY_R -> {
                if ((mods & GLFW.GLFW_MOD_SHIFT) != 0) {
                    createUI();
                }
            }
        }
    }

    protected void keyReleased(int key, int mods) {
    }

    protected void mousePressed(int button, int mods) {
    }

    protected void mouseReleased(int button, int mods) {
    }

    protected void mouseScroll(double xoff, double yoff) {
    }

    public void finish() {
        loader.cleanUp();

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
            if (child != null) {
                child.setDialogType(dialogType);
                childApps.put(dialogType, child);
            }
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

    public static boolean isCircularHueSatField() {
        return circularHueSatField.get();
    }

    public static boolean isHSLColorSpace() {
        return hslColorSpace.get();
    }

    public static void setHSLColorSpace(boolean hslColorSpace) {
        App.hslColorSpace.set(hslColorSpace);
    }

    public static void setCircularHueSatField(boolean circularHueSatField) {
        App.circularHueSatField.set(circularHueSatField);
    }

    public double getFrameRate() {
        return 1.0 / avgFrameTime;
    }

    public void setDialogType(int dialogType) {
        this.dialogType = dialogType;
    }
}