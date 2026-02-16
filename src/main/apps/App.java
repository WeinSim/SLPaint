package main.apps;

import static org.lwjgl.glfw.GLFW.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.function.BooleanSupplier;

import main.MainLoop;
import main.settings.BooleanSetting;
import renderengine.AppRenderer;
import renderengine.Window;
import sutil.math.SVector;
import sutil.ui.KeyboardShortcut;
import sutil.ui.UI;
import sutil.ui.UserAction;
import sutil.ui.elements.UIRoot;
import sutil.ui.elements.UITextInput;
import ui.AppUI;

public sealed abstract class App permits MainApp, ColorEditorApp, SettingsApp, ResizeApp, AboutApp {

    private static final double FRAME_TIME_GAMMA = 0.025;

    /**
     * 0 = normal 1 = mouse above, 2 = always
     */
    private static int debugOutline = 0;
    private static BooleanSetting circularHueSatField = new BooleanSetting("hueSatCircle");
    private static BooleanSetting hslColorSpace = new BooleanSetting("hslColorSpace");

    protected Window window;

    protected boolean[] mouseButtons;
    protected SVector mousePos, prevMousePos;

    private HashMap<String, KeyboardShortcut> keyboardShortcuts;

    private LinkedList<Runnable> eventQueue;

    private AppUI<?> ui;
    protected boolean adjustSizeOnInit = false;

    protected AppRenderer renderer;

    /**
     * Only used if this app is the child app of another app.
     */
    private App parent;
    /**
     * The type of dialog in the parent app that this app represents.
     */
    private int dialogType;
    private HashMap<Integer, App> childApps;

    protected double avgFrameTime = -1;
    protected double avgUpdateTime = -1;
    protected int frameCount = 0;

    public App(int width, int height, int windowMode, String title) {
        this(width, height, windowMode, true, false, title, null);
    }

    public App(int width, int height, int windowMode, boolean resizable, boolean adjustSizeOnInit, String title,
            App parent) {

        this.parent = parent;
        this.adjustSizeOnInit = adjustSizeOnInit;

        MainLoop.addApp(this);

        window = new Window(width, height, windowMode, resizable, title);
        childApps = new HashMap<>();
        keyboardShortcuts = new HashMap<>();
        eventQueue = new LinkedList<>();

        mouseButtons = new boolean[2];
        mousePos = new SVector();
        prevMousePos = null;

        addKeyboardShortcut("cycle_debug", GLFW_KEY_COMMA, 0, App::cycleDebugOutline, false);
        addKeyboardShortcut("reload_shaders", GLFW_KEY_S, GLFW_MOD_SHIFT, () -> renderer.reloadShaders(), true);
        addKeyboardShortcut("reload_ui", GLFW_KEY_R, GLFW_MOD_SHIFT, this::loadUI, true);
    }

    protected void loadUI() {
        ui = createUI();

        if (adjustSizeOnInit) {
            // calling ui.update here to ensure the root has the correct size
            ui.update(mousePos, window.isFocused());

            UIRoot root = UI.getRoot();
            SVector rootSize = root.getSize();
            int width = (int) rootSize.x;
            int height = (int) rootSize.y;
            window.setSizeAndCenter(width, height);
        }
    }

    protected abstract AppUI<?> createUI();

    public void makeContextCurrent() {
        window.makeContextCurrent();
        UI.setContext(ui);
    }

    public void update(double deltaT) {
        long updateStart = System.nanoTime();
        if (avgFrameTime < 0) {
            avgFrameTime = deltaT;
        } else {
            avgFrameTime = (1 - FRAME_TIME_GAMMA) * avgFrameTime + FRAME_TIME_GAMMA * deltaT;
        }
        frameCount++;

        if (ui == null) {
            final String baseStr = "%s has no UI. The UI must be created in the app's constructor using loadUI().\n";
            System.err.format(baseStr, getClass().getName());
            System.exit(1);
        }

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
                case GLFW_PRESS, GLFW_REPEAT -> {
                    if (focus)
                        ui.keyPressed(key, mods);

                    keyPressed(key, mods);
                }
                case GLFW_RELEASE -> {
                    if (focus)
                        ui.keyReleased(key, mods);

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
                case GLFW_PRESS -> {
                    if (0 <= button && button < mouseButtons.length)
                        mouseButtons[button] = true;

                    if (focus)
                        ui.mousePressed(button, mods);

                    mousePressed(button, mods);
                }
                case GLFW_RELEASE -> {
                    if (0 <= button && button < mouseButtons.length)
                        mouseButtons[button] = false;

                    if (focus)
                        ui.mouseReleased(button, mods);

                    mouseReleased(button, mods);
                }
            }
        }

        // process scroll input
        Window.ScrollInfo scrollInfo;
        while ((scrollInfo = window.getNextScrollInfo()) != null) {
            if (focus)
                ui.mouseWheel(new SVector(scrollInfo.xoffset(), scrollInfo.yoffset()), mousePos);

            mouseScroll(scrollInfo.xoffset(), scrollInfo.yoffset());
        }

        // empty event queue
        while (!eventQueue.isEmpty())
            eventQueue.removeFirst().run();

        int[] displaySize = window.getDisplaySize();
        ui.setRootSize(displaySize[0], displaySize[1]);

        ui.update(mousePos, focus);

        window.setCursor(ui.getCursorShape());

        double updateDuration = (System.nanoTime() - updateStart) * 1e-9;
        if (avgUpdateTime < 0) {
            avgUpdateTime = deltaT;
        } else {
            avgUpdateTime = (1 - FRAME_TIME_GAMMA) * avgUpdateTime + FRAME_TIME_GAMMA * updateDuration;
        }
    }

    protected void charInput(char c) {
    }

    protected void keyPressed(int key, int mods) {
        for (KeyboardShortcut shortcut : keyboardShortcuts.values())
            shortcut.keyPressed(key, mods);
    }

    protected void keyReleased(int key, int mods) {
    }

    protected void mousePressed(int button, int mods) {
    }

    protected void mouseReleased(int button, int mods) {
    }

    protected void mouseScroll(double xoff, double yoff) {
    }

    public void exit() {
        window.requestClose();
    }

    public void finish() {
        renderer.cleanUp();

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
        }
    }

    protected abstract App createChildApp(int dialogType);

    public void clearChildApp(int dialogType) {
        childApps.remove(dialogType);
    }

    public final void render() {
        if (renderer == null) {
            renderer = new AppRenderer(this);
        }
        renderer.render();
    }

    public void queueEvent(Runnable action) {
        eventQueue.add(action);
    }

    /**
     * When text is set to false, the shortcut will not run if a text input is
     * currently active.
     */
    public void addKeyboardShortcut(String identifier, int key, int modifiers, Runnable action, boolean text) {
        UserAction userAction = new UserAction(action,
                text
                        ? () -> !(UI.getSelectedElement() instanceof UITextInput)
                        : () -> true);
        addKeyboardShortcut(new KeyboardShortcut(identifier, key, modifiers, userAction));
    }

    public void addKeyboardShortcut(String identifier, int key, int modifiers, Runnable action,
            BooleanSupplier isPossible) {

        UserAction userAction = new UserAction(action, isPossible);
        addKeyboardShortcut(new KeyboardShortcut(identifier, key, modifiers, userAction));
    }

    public void addKeyboardShortcut(KeyboardShortcut shortcut) {
        String identifier = shortcut.getIdentifier();
        KeyboardShortcut currentShortcut = keyboardShortcuts.get(identifier);
        if (currentShortcut != null)
            System.err.format("Duplicate keyboard shortcut \"%s\" in %s\n", identifier, getClass().getName());
        keyboardShortcuts.put(shortcut.getIdentifier(), shortcut);
    }

    public KeyboardShortcut getKeyboardShortcut(String identifier) {
        KeyboardShortcut k = keyboardShortcuts.get(identifier);
        if (k == null)
            throw new RuntimeException(String.format("Keyboard shortcut \"%s\" does not exist", identifier));
        return k;
    }

    public Window getWindow() {
        return window;
    }

    public SVector getWindowSize() {
        int[] size = window.getDisplaySize();
        return new SVector(size[0], size[1]);
    }

    public AppUI<?> getUI() {
        return ui;
    }

    public static void cycleDebugOutline() {
        debugOutline = (debugOutline + 1) % 3;
    }

    public static int getDebugOutline() {
        return debugOutline;
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

    public double getAvgUpdateTime() {
        return avgUpdateTime;
    }

    public int getFrameCount() {
        return frameCount;
    }

    public void setDialogType(int dialogType) {
        this.dialogType = dialogType;
    }
}