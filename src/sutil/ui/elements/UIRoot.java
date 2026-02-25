package sutil.ui.elements;

import java.util.ArrayList;

import sutil.math.SVector;

public class UIRoot extends UIContainer {

    private int minLayer, maxLayer;

    public UIRoot(int orientation, int alignment) {
        super(orientation, alignment);

        backgroundNormal = true;
        outlineNormal = false;
    }

    @Override
    public void updateVisibility() {
        super.updateVisibility();

        minLayer = relativeLayer;
        maxLayer = relativeLayer;

        setMinMaxLayer(this, relativeLayer);
    }

    private void setMinMaxLayer(UIContainer parent, int currentLayer) {
        for (UIElement child : parent.getChildren()) {
            int childLayer = currentLayer + child.getRelativeLayer();
            minLayer = Math.min(minLayer, childLayer);
            maxLayer = Math.max(maxLayer, childLayer);

            if (child instanceof UIContainer container)
                setMinMaxLayer(container, childLayer);
        }
    }

    public void updateMouseAbove(boolean valid) {
        int currentLayer = relativeLayer;
        // System.out.format("min = %d, max = %d\n", minLayer, maxLayer);
        for (int targetLayer = maxLayer; targetLayer >= minLayer; targetLayer--) {
            valid &= !super.updateMouseAbove(valid, true, currentLayer, targetLayer);
        }
    }

    public void updateSize() {
        setMinSize();
        setPreferredSize();
        expandAsNeccessary();
        positionChildren();
    }

    public ArrayList<UIElement> getSelectableElements() {
        UIModalDialog modalDialog = getModalDialog();
        return getSelectableElements((modalDialog != null ? modalDialog : this), new ArrayList<UIElement>());
    }

    private static ArrayList<UIElement> getSelectableElements(UIContainer parent, ArrayList<UIElement> elements) {
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

    public UIModalDialog getModalDialog() {
        for (UIElement child : getChildren()) {
            if (child instanceof UIModalDialog m)
                return m;
        }
        return null;
    }

    public boolean hasModalDialog() {
        return getModalDialog() != null;
    }

    @Override
    public SVector getAbsolutePosition() {
        return new SVector();
    }
}