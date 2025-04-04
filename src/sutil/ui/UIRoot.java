package sutil.ui;

import java.util.ArrayList;

import sutil.math.SVector;

public class UIRoot extends UIContainer {

    private ArrayList<UIFloatContainer> floatContainers;

    public UIRoot(UIPanel panel, int orientation, int alignment) {
        super(orientation, alignment);
        this.panel = panel;

        panel.setRoot(this);

        backgroundNormal = true;
        outlineNormal = false;

        floatContainers = new ArrayList<>();
    }

    @Override
    public void determineChildVisibility() {
        super.determineChildVisibility();

        floatContainers.clear();
        addFloatContainers(this);
    }

    private void addFloatContainers(UIContainer parent) {
        for (UIElement child : parent.getChildren()) {
            if (child instanceof UIFloatContainer floatContainer) {
                floatContainers.add(floatContainer);
            }

            if (child instanceof UIContainer container) {
                addFloatContainers(container);
            }
        }
    }

    @Override
    public void updateMousePosition(SVector mouse, boolean valid) {
        updateMouseAboveReference(mouse, valid);
        mousePosition.set(mouse);

        boolean floatMouseAbove = false;
        for (UIFloatContainer floatContainer : floatContainers) {
            SVector relativeMouse = floatContainer.parent.getAbsolutePosition().scale(-1).add(mouse);
            floatContainer.updateMousePosition(relativeMouse, valid && !floatMouseAbove);
            floatMouseAbove |= floatContainer.mouseAbove();
        }

        super.updateMousePosition(mouse, valid && !floatMouseAbove);
    }

    public void updateSize() {
        // updateSizeReferences();
        setMinSize();
        setPreferredSize();
        expandAsNeccessary();
        positionChildren();
    }

    @Override
    public SVector getAbsolutePosition() {
        return new SVector();
    }
}