package sutil.ui;

import java.util.ArrayList;
import java.util.LinkedList;

import org.lwjgl.glfw.GLFW;

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

    /**
     * Indicates wether the left mouse button is currently being pressed.
     */
    private boolean mousePressed;

    private LinkedList<UIAction> eventQueue;

    public UIPanel() {
        selectedElement = null;
        dragging = false;
        mousePressed = false;

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
            selectedElement = null;
        }

        root.updateMousePosition(mousePos, valid && !dragging);

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
            if (mouseButton == LEFT) {
                mousePressed = true;
            }
            selectedElement = null;
            root.mousePressed(mouseButton);
        });
    }

    public void mouseReleased(int mouseButton, int mods) {
        if (mouseButton == LEFT) {
            queueEvent(() -> mousePressed = false);
        }
    }

    public void charInput(char c) {
        queueEvent(() -> {
            root.charInput(c);
        });
    }

    public void keyPressed(int key, int mods) {
        queueEvent(() -> {
            if (key == GLFW.GLFW_KEY_TAB) {
                cycleSelectedElement((mods & GLFW.GLFW_MOD_SHIFT) != 0);
            } else if (key == GLFW.GLFW_KEY_ENTER) {
                if (selectedElement != null) {
                    UIAction clickAction = selectedElement.getLeftClickAction();
                    if (clickAction != null) {
                        clickAction.run();
                    }
                }
            }
            root.keyPressed(key);
        });
    }

    public void mouseWheel(SVector scroll, SVector mousePos) {
        queueEvent(() -> root.mouseWheel(scroll.copy().scale(mouseWheelSensitivity), mousePos));
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
            selectedElement = backwards ? elements.getLast() : elements.getFirst();
        } else {
            int oldIndex = elements.indexOf(selectedElement);
            int newIndex = (oldIndex + (backwards ? -1 : 1) + elements.size()) % elements.size();
            selectedElement = elements.get(newIndex);
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

    public abstract double textWidth(String text, double textSize, String fontName);

    public double getMargin() {
        return margin;
    }

    public double getPadding() {
        return padding;
    }

    public SVector getDefaultTextColor() {
        return new SVector(1, 1, 1).scale(1.0);
    }

    public SVector getBackgroundNormalColor() {
        return new SVector(1, 1, 1).scale(0.1);
    }

    public SVector getBackgroundHighlightColor() {
        return new SVector(1, 1, 1).scale(0.2);
    }

    public SVector getOutlineNormalColor() {
        return new SVector(1, 1, 1).scale(0.6);
    }

    public SVector getOutlineHighlightColor() {
        return new SVector(1, 1, 1).scale(0.75);
    }

    public SVector getSeparatorColor() {
        return new SVector(1, 1, 1).scale(0.15);
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

    public void setSelectedElement(UIElement element) {
        selectedElement = element;
    }

    public UIElement getSelectedElement() {
        return selectedElement;
    }

    public boolean isMousePressed() {
        return mousePressed;
    }

    public void setRoot(UIRoot root) {
        this.root = root;
    }
}