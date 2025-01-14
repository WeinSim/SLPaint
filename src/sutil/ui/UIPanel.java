package sutil.ui;

import java.util.ArrayList;

import org.lwjgl.glfw.GLFW;

import sutil.math.SVector;

public abstract class UIPanel {

    protected UIRoot root;

    protected double mouseWheelSensitivity = 100;

    protected double textSize = 32;

    /**
     * Space around the outside
     */
    protected double margin = 10;

    /**
     * Space between children
     */
    protected double padding = 10;

    private UIElement selectedElement;

    public UIPanel() {
        selectedElement = null;
    }

    public void update(SVector mousePos) {
        root.update(mousePos);

        updateSize();
    }

    protected void updateSize() {
        root.updateSizeReferences();
        root.setMinSize();
        root.expandAsNeccessary(null);
        root.positionChildren();
    }

    public void mousePressed(SVector mousePos) {
        selectedElement = null;
        root.mousePressed(mousePos);
    }

    public void keyPressed(char key, boolean shift) {
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
        root.keyPressed(key);
    }

    public void mouseWheel(double scroll, SVector mousePos) {
        root.mouseWheel(scroll * mouseWheelSensitivity, mousePos);
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
        return getSelectableElements(root);
    }

    private ArrayList<UIElement> getSelectableElements(UIContainer parent) {
        ArrayList<UIElement> elements = new ArrayList<>();
        for (UIElement child : parent.getChildren()) {
            if (child.isSelectable()) {
                elements.add(child);
            }
            if (child instanceof UIContainer container) {
                elements.addAll(getSelectableElements(container));
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

    public void setRoot(UIRoot root) {
        this.root = root;
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
        return new SVector(1, 1, 1).scale(0.6);
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
}