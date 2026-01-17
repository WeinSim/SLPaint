package sutil.ui;

import java.util.ArrayList;
import java.util.LinkedList;

import org.lwjgl.glfw.GLFW;
import org.lwjglx.util.vector.Vector4f;

import sutil.math.SVector;

public abstract class UIPanel {

    /**
     * Mouse buttons
     */
    public static final int LEFT = 0, RIGHT = 1;

    private double uiScale = 1.0;

    // /**
    // * Space around the outside
    // */
    // protected double margin = 10;

    // /**
    // * Space between children
    // */
    // protected double padding = 10;

    // protected double defaultTextSize = 32;
    // protected double smallTextSize = 26;

    protected String defaultFontName = "Courier New Bold";

    protected double mouseWheelSensitivity = 100;

    protected UIRoot root;
    private UIElement selectedElement;
    /**
     * Used to set selectedElement to null if the selected element is not currently
     * visible.
     */
    private boolean selectedElementVisible;
    private boolean dragging;

    private boolean leftMousePressed;
    private boolean rightMousePressed;

    private LinkedList<UIAction> eventQueue;

    public UIPanel(SVector initialRootSize) {
        selectedElement = null;
        dragging = false;
        leftMousePressed = false;
        rightMousePressed = false;

        eventQueue = new LinkedList<>();

        root = new UIRoot(this, UIContainer.VERTICAL, UIContainer.LEFT);
        root.zeroMargin().zeroPadding().noOutline().withBackground();
        root.setFixedSize(initialRootSize);

        init();
    }

    protected abstract void init();

    public void update(SVector mousePos, boolean valid) {
        root.lock();

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

    public void mousePressed(int mouseButton, int mods) {
        queueEvent(() -> {
            switch (mouseButton) {
                case GLFW.GLFW_MOUSE_BUTTON_LEFT -> leftMousePressed = true;
                case GLFW.GLFW_MOUSE_BUTTON_RIGHT -> rightMousePressed = true;
            }
            select(null);
            root.mousePressed(mouseButton, mods);
        });
    }

    public void mouseReleased(int mouseButton, int mods) {
        queueEvent(() -> {
            switch (mouseButton) {
                case GLFW.GLFW_MOUSE_BUTTON_LEFT -> leftMousePressed = false;
                case GLFW.GLFW_MOUSE_BUTTON_RIGHT -> rightMousePressed = false;
            }
            root.mouseReleased(mouseButton, mods);
        });
    }

    public void charInput(char c) {
        queueEvent(() -> {
            root.charInput(c);
        });
    }

    public void keyPressed(int key, int mods) {
        queueEvent(() -> {
            switch (key) {
                case GLFW.GLFW_KEY_TAB -> cycleSelectedElement((mods & GLFW.GLFW_MOD_SHIFT) != 0);
                case GLFW.GLFW_KEY_CAPS_LOCK -> select(null);
                case GLFW.GLFW_KEY_ENTER -> {
                    if (selectedElement != null) {
                        UIAction clickAction = selectedElement.getLeftClickAction();
                        if (clickAction != null) {
                            clickAction.run();
                        }
                    }
                }
            }
            root.keyPressed(key, mods);
        });
    }

    public void mouseWheel(SVector scroll, SVector mousePos, int mods) {
        queueEvent(() -> root.mouseWheel(scroll.copy().scale(mouseWheelSensitivity), mousePos, mods));
    }

    public void setRootSize(int width, int height) {
        root.setFixedSize(new SVector(width, height));
    }

    public void queueEvent(UIAction action) {
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

    public void select(UIElement element) {
        select(element, null);
    }

    public void select(UIElement element, SVector mouse) {
        selectedElement = element;
        if (element != null) {
            element.select(mouse);
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

    public boolean mouseAboveTextInput() {
        return mouseAboveTextInput(root);
    }

    private boolean mouseAboveTextInput(UIElement element) {
        if (element instanceof UITextInput && element.mouseAbove()) {
            return true;
        }
        if (element instanceof UIContainer container) {
            for (UIElement child : container.getChildren()) {
                if (mouseAboveTextInput(child)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * During the updateVisibility() step of update(), the currently selected
     * element has to report to the UIPanel that it is still visible.
     */
    void confirmSelectedElement() {
        selectedElementVisible = true;
    }

    void setDragging() {
        dragging = true;
    }

    public double textWidth(String text, double textSize, String fontName) {
        return textWidth(text, textSize, fontName, text.length());
    }

    public abstract double textWidth(String text, double textSize, String fontName, int len);

    public abstract int getCharIndex(String text, double textSize, String fontName, double x);

    public double get(UISizes s) {
        return getSize(s.size, s.forceInteger);
    }

    private double getSize(double s, boolean forceInteger) {
        double size = s * uiScale;
        if (forceInteger) {
            size = (int) Math.round(size);
        }
        return size;
    }

    public SVector getWidthHeight(UISizes s) {
        return new SVector(getSize(s.width, s.forceInteger), getSize(s.height, s.forceInteger));
    }

    protected abstract boolean isDarkMode();

    protected abstract Vector4f getBaseColor();

    public Vector4f get(UIColors c) {
        // This method is being called ~5520 times per second (which is too much!)
        // Maybe cache the results?

        boolean darkMode = isDarkMode();
        if (c.useBrightness) {
            double brightness = darkMode ? c.darkModeBrightness : c.lightModeBrightness;
            Vector4f ret = (Vector4f) new Vector4f(getBaseColor()).scale((float) brightness);
            ret.w = 1.0f;
            return ret;
        } else {
            return darkMode ? c.darkColor : c.lightColor;
        }
    }

    public String getDefaultFontName() {
        return defaultFontName;
    }

    public UIElement getSelectedElement() {
        return selectedElement;
    }

    public boolean isLeftMousePressed() {
        return leftMousePressed;
    }

    public boolean isRightMousePressed() {
        return rightMousePressed;
    }

    public void setRoot(UIRoot root) {
        this.root = root;
    }

    public UIRoot getRoot() {
        return root;
    }

    public void setUIScale(double uiScale) {
        this.uiScale = uiScale;
    }

    public double getUIScale() {
        return uiScale;
    }
}