package sutil.ui;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.function.Consumer;

import org.lwjgl.glfw.GLFW;

import sutil.math.SVector;

public abstract class UIPanel {

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

    protected ArrayList<UIFloatContainer> floatElements;

    private boolean mousePressed;

    private LinkedList<UIAction> eventQueue;

    public UIPanel() {
        selectedElement = null;
        mousePressed = false;

        floatElements = new ArrayList<>();
        eventQueue = new LinkedList<>();
    }

    public void update(SVector mousePos) {
        while (!eventQueue.isEmpty()) {
            eventQueue.removeFirst().run();
        }

        forAllContainers(container -> container.determineChildVisibility());

        boolean mouseAboveFloat = false;
        for (UIElement floatElement : floatElements) {
            floatElement.update(mousePos);
            mouseAboveFloat |= floatElement.mouseAbove();
        }
        if (mouseAboveFloat) {
            mousePos = null;
        }
        root.update(mousePos);

        updateSize();
    }

    protected void updateSize() {
        forAllContainers(container -> {
            container.updateSizeReferences();
            container.setMinSize();
            container.expandAsNeccessary(null);
            container.positionChildren();
        });
    }

    public void mousePressed(SVector mousePos) {
        eventQueue.add(() -> {
            selectedElement = null;
            mousePressed = true;
            forAllElements(element -> element.mousePressed(mousePos));
        });
    }

    public void mouseReleased() {
        eventQueue.add(() -> mousePressed = false);
    }

    public void keyPressed(char key, boolean shift) {
        eventQueue.add(() -> {
            if (key == GLFW.GLFW_KEY_TAB) {
                cycleSelectedElement(shift);
            } else if (key == GLFW.GLFW_KEY_ENTER) {
                if (selectedElement != null) {
                    UIAction clickAction = selectedElement.getClickAction();
                    if (clickAction != null) {
                        clickAction.run();
                    }
                }
            }
            forAllElements(element -> element.keyPressed(key));
        });
    }

    public void mouseWheel(SVector scroll, SVector mousePos) {
        eventQueue.add(() -> forAllElements(
                element -> element.mouseWheel(scroll.copy().scale(mouseWheelSensitivity), mousePos)));
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

    public void addFloatContainer(UIFloatContainer container) {
        floatElements.add(container);
        container.setPanel(this);
    }

    public void removeFloatContainer(UIElement element) {
        floatElements.remove(element);
    }

    private void forAllElements(Consumer<? super UIElement> action) {
        action.accept(root);
        floatElements.forEach(action);
    }

    private void forAllContainers(Consumer<? super UIContainer> action) {
        action.accept(root);
        floatElements.forEach(action);
    }

    public UIRoot getRoot() {
        return root;
    }

    public ArrayList<UIFloatContainer> getFloatElements() {
        return floatElements;
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