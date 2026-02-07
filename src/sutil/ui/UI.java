package sutil.ui;

import static org.lwjgl.glfw.GLFW.*;

import java.util.ArrayList;
import java.util.LinkedList;

import org.lwjglx.util.vector.Vector4f;

import sutil.math.SVector;
import sutil.ui.elements.UIContainer;
import sutil.ui.elements.UIElement;
import sutil.ui.elements.UIRoot;

public abstract class UI {

    public static final int LEFT = 0, TOP = 0, CENTER = 1, RIGHT = 2, BOTTOM = 2;
    public static final int VERTICAL = 0, HORIZONTAL = 1, NONE = 2, BOTH = 3;

    private static UI context = null;

    protected String defaultFontName = "FreeMonoBold";

    private double uiScale = 1.0;

    protected double mouseWheelSensitivity = 100;

    protected UIRoot root;
    private UIElement selectedElement;
    /**
     * Used to set selectedElement to null if the selected element is not currently
     * visible.
     */
    private boolean selectedElementVisible;
    private boolean dragging;

    /**
     * Contains the flags of the currently held down modifier keys (Shift, Control,
     * Alt).
     */
    private int modifiers;
    private boolean leftMousePressed;
    private boolean rightMousePressed;

    private LinkedList<Runnable> eventQueue;

    public UI(double uiScale, SVector initialRootSize) {
        setContext(this);

        selectedElement = null;
        dragging = false;
        modifiers = 0;
        leftMousePressed = false;
        rightMousePressed = false;

        eventQueue = new LinkedList<>();

        this.uiScale = uiScale;

        root = new UIRoot(VERTICAL, LEFT);
        root.zeroMargin().zeroPadding().noOutline().withBackground();
        root.setFixedSize(initialRootSize);

        init();
    }

    protected abstract void init();

    public void update(SVector mousePos, boolean valid) {
        while (!eventQueue.isEmpty()) {
            eventQueue.removeFirst().run();
        }

        selectedElementVisible = false;
        root.updateVisibility();
        if (!selectedElementVisible) {
            select(null);
        }

        root.updateMousePosition(mousePos);
        root.updateMouseAbove(valid && !dragging);

        // The dragging variable lags one frame behind (because it is being used before
        // it is being set)
        dragging = false;
        root.update();

        root.updateSize();
    }

    public void keyPressed(int key, int mods) {
        modifiers |= switch (key) {
            case GLFW_KEY_LEFT_CONTROL, GLFW_KEY_RIGHT_CONTROL -> GLFW_MOD_CONTROL;
            case GLFW_KEY_LEFT_SHIFT, GLFW_KEY_RIGHT_SHIFT -> GLFW_MOD_SHIFT;
            case GLFW_KEY_LEFT_ALT, GLFW_KEY_RIGHT_ALT -> GLFW_MOD_ALT;
            default -> 0;
        };

        queueEvent(() -> {
            switch (key) {
                case GLFW_KEY_TAB -> cycleSelectedElement((mods & GLFW_MOD_SHIFT) != 0);
                case GLFW_KEY_ESCAPE -> select(null);
                case GLFW_KEY_ENTER -> {
                    if (selectedElement != null) {
                        Runnable clickAction = selectedElement.getLeftClickAction();
                        if (clickAction != null) {
                            clickAction.run();
                        }
                    }
                }
            }
            root.keyPressed(key, mods);
        });
    }

    public void keyReleased(int key, int mods) {
        modifiers ^= 0xFFFFFFFF;
        modifiers |= switch (key) {
            case GLFW_KEY_LEFT_CONTROL, GLFW_KEY_RIGHT_CONTROL -> GLFW_MOD_CONTROL;
            case GLFW_KEY_LEFT_SHIFT, GLFW_KEY_RIGHT_SHIFT -> GLFW_MOD_SHIFT;
            case GLFW_KEY_LEFT_ALT, GLFW_KEY_RIGHT_ALT -> GLFW_MOD_ALT;
            default -> 0;
        };
        modifiers ^= 0xFFFFFFFF;

        root.keyPressed(key, mods);
    }

    public void mousePressed(int mouseButton, int mods) {
        queueEvent(() -> {
            switch (mouseButton) {
                case GLFW_MOUSE_BUTTON_LEFT -> leftMousePressed = true;
                case GLFW_MOUSE_BUTTON_RIGHT -> rightMousePressed = true;
            }
            select(null);
            root.mousePressed(mouseButton, mods);
        });
    }

    public void mouseReleased(int mouseButton, int mods) {
        queueEvent(() -> {
            switch (mouseButton) {
                case GLFW_MOUSE_BUTTON_LEFT -> leftMousePressed = false;
                case GLFW_MOUSE_BUTTON_RIGHT -> rightMousePressed = false;
            }
            root.mouseReleased(mouseButton, mods);
        });
    }

    public void charInput(char c) {
        queueEvent(() -> root.charInput(c));
    }

    public void mouseWheel(SVector scroll, SVector mousePos) {
        queueEvent(() -> root.mouseWheel(scroll.copy().scale(mouseWheelSensitivity), mousePos, modifiers));
    }

    public void setRootSize(int width, int height) {
        root.setFixedSize(new SVector(width, height));
    }

    private void queueEvent(Runnable action) {
        eventQueue.add(action);
    }

    private void cycleSelectedElement(boolean backwards) {
        ArrayList<UIElement> elements = getSelectableElements();
        if (elements.isEmpty()) {
            return;
        }
        if (selectedElement == null) {
            select(backwards ? elements.getLast() : elements.getFirst());
        } else {
            int oldIndex = elements.indexOf(selectedElement);
            int newIndex = (oldIndex + (backwards ? -1 : 1) + elements.size()) % elements.size();
            select(elements.get(newIndex));
        }
    }

    private ArrayList<UIElement> getSelectableElements() {
        return getSelectableElements(root, new ArrayList<UIElement>());
    }

    private ArrayList<UIElement> getSelectableElements(UIContainer parent, ArrayList<UIElement> elements) {
        for (UIElement child : parent.getChildren()) {
            if (child.isSelectable()) {
                elements.add(child);
            }
            if (child instanceof UIContainer container) {
                getSelectableElements(container, elements);
            }
        }
        return elements;
    }

    public int getCursorShape() {
        Integer shape = root.getCursorShape();
        return shape == null ? GLFW_ARROW_CURSOR : shape;
    }

    // Statically available methods

    public static void select(UIElement element) {
        context.selectImpl(element);
    }

    public void selectImpl(UIElement element) {
        select(element, null);
    }

    public static void select(UIElement element, SVector mouse) {
        context.selectImpl(element, mouse);
    }

    public void selectImpl(UIElement element, SVector mouse) {
        selectedElement = element;
        if (element != null) {
            element.select(mouse);
        }
    }

    /**
     * During the updateVisibility() step of update(), the currently selected
     * element has to report to the UIPanel that it is still visible.
     */
    public static void confirmSelectedElement() {
        context.confirmSelectedElementImpl();
    }

    protected void confirmSelectedElementImpl() {
        selectedElementVisible = true;
    }

    public static void setDragging() {
        context.setDraggingImpl();
    }

    protected void setDraggingImpl() {
        dragging = true;
    }

    public static double textWidth(String text, double textSize, String fontName) {
        return textWidth(text, textSize, fontName, text.length());
    }

    public static double textWidth(String text, double textSize, String fontName, int len) {
        return context.textWidthImpl(text, textSize, fontName, len);
    }

    public abstract double textWidthImpl(String text, double textSize, String fontName, int len);

    public static int getCharIndex(String text, double textSize, String fonrName, double x) {
        return context.getCharIndexImpl(text, textSize, fonrName, x);
    }

    public abstract int getCharIndexImpl(String text, double textSize, String fontName, double x);

    public static boolean isDarkMode() {
        return context.isDarkModeImpl();
    }

    protected abstract boolean isDarkModeImpl();

    public static Vector4f getBaseColor() {
        return context.getBaseColorImpl();
    }

    protected abstract Vector4f getBaseColorImpl();

    public static double getUIScale() {
        return context.uiScale;
    }

    public static String getDefaultFontName() {
        return context.getDefaultFontNameImpl();
    }

    public String getDefaultFontNameImpl() {
        return defaultFontName;
    }

    public static UIElement getSelectedElement() {
        return context.getSelectedElementImpl();
    }

    public UIElement getSelectedElementImpl() {
        return selectedElement;
    }

    public static int getModifiers() {
        return context.modifiers;
    }

    public static boolean isLeftMousePressed() {
        return context.isLeftMousePressedImpl();
    }

    public boolean isLeftMousePressedImpl() {
        return leftMousePressed;
    }

    public static boolean isRightMousePressed() {
        return context.isRightMousePressedImpl();
    }

    public boolean isRightMousePressedImpl() {
        return rightMousePressed;
    }

    public void setRoot(UIRoot root) {
        this.root = root;
    }

    public static UIRoot getRoot() {
        return context.getRootImpl();
    }

    public UIRoot getRootImpl() {
        return root;
    }

    public void setUIScale(double uiScale) {
        this.uiScale = uiScale;
    }

    public static void setContext(UI context) {
        UI.context = context;
    }

    public static UI getContext() {
        return context;
    }
}