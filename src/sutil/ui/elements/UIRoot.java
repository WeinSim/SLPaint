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

        UIModalDialog modalDialog = getModalDialog();
        if (modalDialog != null && !modalDialog.isVisible())
            remove(modalDialog);
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

    @Override
    public void keyPressed(int key, int mods) {
        UIModalDialog modalDialog = getModalDialog();
        if (modalDialog != null)
            modalDialog.keyPressed(key, mods);
        else
            super.keyPressed(key, mods);
    }

    @Override
    public void charInput(char c) {
        UIModalDialog modalDialog = getModalDialog();
        if (modalDialog != null)
            modalDialog.charInput(c);
        else
            super.charInput(c);
    }

    @Override
    public void mousePressed(int mouseButton, int mods) {
        UIModalDialog modalDialog = getModalDialog();
        if (modalDialog != null)
            modalDialog.mousePressed(mouseButton, mods);
        else
            super.mousePressed(mouseButton, mods);
    }

    @Override
    public void mouseReleased(int mouseButton, int mods) {
        UIModalDialog modalDialog = getModalDialog();
        if (modalDialog != null)
            modalDialog.mouseReleased(mouseButton, mods);
        else
            super.mouseReleased(mouseButton, mods);
    }

    @Override
    public boolean mouseWheel(SVector scroll, SVector mousePos, int mods) {
        UIModalDialog modalDialog = getModalDialog();
        if (modalDialog != null)
            return modalDialog.mouseWheel(scroll, mousePos, mods);
        else
            return super.mouseWheel(scroll, mousePos, mods);
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
        
    private UIModalDialog getModalDialog() {
        for (UIElement child: getChildren()) {
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