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

    /**
     * Space around the outside
     */
    protected double margin = 10;

    /**
     * Space between children
     */
    protected double padding = 10;

    protected double defaultTextSize = 32;
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

    public UIPanel() {
        selectedElement = null;
        dragging = false;
        leftMousePressed = false;
        rightMousePressed = false;

        eventQueue = new LinkedList<>();
    }

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

    public UIRoot getRoot() {
        return root;
    }

    public double textWidth(String text, double textSize, String fontName) {
        return textWidth(text, textSize, fontName, text.length());
    }

    public abstract double textWidth(String text, double textSize, String fontName, int len);

    public abstract int getCharIndex(String text, double textSize, String fontName, double x);

    public double getMargin() {
        return margin;
    }

    public double getPadding() {
        return padding;
    }

    public Vector4f getDefaultTextColor() {
        float brightness = 1.0f;
        return new Vector4f(brightness, brightness, brightness, 1.0f);
    }

    public Vector4f getBackgroundNormalColor() {
        float brightness = 0.1f;
        return new Vector4f(brightness, brightness, brightness, 1.0f);
    }

    public Vector4f getBackgroundHighlightColor() {
        float brightness = 0.2f;
        return new Vector4f(brightness, brightness, brightness, 1.0f);
    }

    public Vector4f getStrokeNormalColor() {
        float brightness = 0.6f;
        return new Vector4f(brightness, brightness, brightness, 1.0f);
    }

    public Vector4f getStrokeHighlightColor() {
        float brightness = 0.75f;
        return new Vector4f(brightness, brightness, brightness, 1.0f);
    }

    public Vector4f getSeparatorColor() {
        float brightness = 0.15f;
        return new Vector4f(brightness, brightness, brightness, 1.0f);
    }

    public double getStrokeWeight() {
        return 1.0;
    }

    public double getDefaultTextSize() {
        return defaultTextSize;
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
}