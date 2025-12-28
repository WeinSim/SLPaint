package sutil.ui;

import sutil.math.SVector;

public class UIRoot extends UIContainer {

    private int minLayer, maxLayer;

    public UIRoot(UIPanel panel, int orientation, int alignment) {
        super(orientation, alignment);
        this.panel = panel;

        panel.setRoot(this);

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

            if (child instanceof UIContainer container) {
                setMinMaxLayer(container, childLayer);
            }
        }
    }

    public void updateMouseAbove(boolean valid) {
        int currentLayer = relativeLayer;
        // System.out.format("min = %d, max = %d\n", minLayer, maxLayer);
        valid = true;
        for (int targetLayer = maxLayer; targetLayer >= minLayer; targetLayer--) {
            valid &= !super.updateMouseAbove(valid, true, currentLayer, targetLayer);

            // SVector relativeMouse =
            // floatContainer.parent.getAbsolutePosition().scale(-1).add(mouse);
        }
    }

    public void updateSize() {
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