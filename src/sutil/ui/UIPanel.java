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

    protected double textSize = 32;

    protected double mouseWheelSensitivity = 100;

    protected UIRoot root;
    private UIElement selectedElement;
    /**
     * Used to set selectedElement to null if the selected element is not currently
     * visible.
     */
    private boolean selectedElementVisible;

    /**
     * Indicates wether the left mouse button is currently being pressed.
     */
    private boolean mousePressed;

    private LinkedList<UIAction> eventQueue;

    public UIPanel() {
        selectedElement = null;
        mousePressed = false;

        eventQueue = new LinkedList<>();
    }

    public void update(SVector mousePos, boolean valid) {
        root.lock();

        selectedElementVisible = false;
        root.updateVisibility();
        if (!selectedElementVisible) {
            selectedElement = null;
        }

        root.updateMousePosition(mousePos, valid);

        while (!eventQueue.isEmpty()) {
            eventQueue.removeFirst().run();
        }

        root.update();

        root.updateSize();
    }

    /**
     * During the updateVisibility() step of update(), the currently
     * selected element has to report to the UIPanel that it is still visible.
     */
    void confirmSelectedElement() {
        selectedElementVisible = true;
    }

    public void mousePressed(int mouseButton) {
        queueEvent(() -> {
            if (mouseButton == LEFT) {
                mousePressed = true;
            }
            selectedElement = null;
            root.mousePressed(mouseButton);
        });
    }

    public void mouseReleased(int mouseButton) {
        if (mouseButton == LEFT) {
            queueEvent(() -> mousePressed = false);
        }
    }

    public void keyPressed(char key, boolean shift) {
        queueEvent(() -> {
            if (key == GLFW.GLFW_KEY_TAB) {
                cycleSelectedElement(shift);
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

    public abstract double textWidth(String text);

    public double getTextSize() {
        return textSize;
    }

    public double getMargin() {
        return margin;
    }

    public double getPadding() {
        return padding;
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