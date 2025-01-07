package sutil.ui;

import sutil.SUtil;
import sutil.math.SVector;

public abstract class UIElement {

    protected SVector position;
    protected SVector size;
    protected UIContainer parent;

    protected UIPanel panel;

    protected boolean outlineNormal = false;
    protected boolean outlineHighlight = false;
    protected boolean backgroundNormal = false;
    protected boolean backgroundHighlight = false;

    protected boolean mouseAbove;
    protected UIAction clickAction = null;
    protected boolean selectOnClick = false;
    protected boolean selectable = false;

    protected UIStyle style;

    public UIElement() {
        position = new SVector();
        size = new SVector();

        setDefaultStyle();
    }

    public void update(SVector mouse) {
        mouseAbove = SUtil.pointInsideRect(mouse, position, size);
    }

    public void mousePressed(SVector mouse) {
        if (mouseAbove) {
            if (clickAction != null) {
                clickAction.run();
            }
            if (selectOnClick) {
                panel.setSelectedElement(this);
            }
        }
    }

    public void keyPressed(char key) {

    }

    public boolean isSelectable() {
        return selectable;
    }

    public void setSelectable(boolean selectable) {
        this.selectable = selectable;
    }

    /**
     * basically the old updateSize() (before different SizeTypes existed)
     */
    public abstract void setMinSize();

    public SVector getSize() {
        return size;
    }

    protected void setPanel(UIPanel panel) {
        this.panel = panel;
    }

    public SVector getPosition() {
        return position;
    }

    public boolean mouseAbove() {
        return mouseAbove;
    }

    public void setClickAction(UIAction clickAction) {
        this.clickAction = clickAction;
    }

    public UIAction getClickAction() {
        return clickAction;
    }

    public SVector getAbsolutePosition() {
        return parent.getAbsolutePosition().add(position);
    }

    public final SVector getBackgroundColor() {
        return style.getBackgroundColor();
    }

    public final SVector getOutlineColor() {
        SVector ol = style.getOutlineColor();
        if (ol == null && panel.getSelectedElement() == this) {
            ol = panel.getOutlineNormalColor();
        }
        return ol;
    }

    public final double getStrokeWeight() {
        double sw = style.getStrokeWeight();
        if (panel.getSelectedElement() == this) {
            if (style.getOutlineColor() == null) {
                sw = panel.getStrokeWeight();
            } else {
                sw *= 2;
            }
        }
        return sw;
    }

    public void setOutlineNormal(boolean outlineNormal) {
        this.outlineNormal = outlineNormal;
    }

    public void setOutlineHighlight(boolean outlineHighlight) {
        this.outlineHighlight = outlineHighlight;
    }

    public void setBackgroundNormal(boolean backgroundNormal) {
        this.backgroundNormal = backgroundNormal;
    }

    public void setBackgroundHighlight(boolean backgroundHighlight) {
        this.backgroundHighlight = backgroundHighlight;
    }

    public UIElement noOutline() {
        setOutlineNormal(false);
        setOutlineHighlight(false);
        return this;
    }

    public UIElement noBackground() {
        setBackgroundNormal(false);
        setBackgroundHighlight(false);
        return this;
    }

    public UIElement withOutline() {
        setOutlineNormal(true);
        setOutlineHighlight(false);
        return this;
    }

    public UIElement withBackground() {
        setBackgroundNormal(true);
        setBackgroundHighlight(false);
        return this;
    }

    public void setStyle(UIStyle style) {
        this.style = style;
    }

    public void setDefaultStyle() {
        UIGetter<SVector> backgroundColorGetter = () -> {
            SVector bgColor = null;
            if (backgroundNormal) {
                bgColor = panel.getBackgroundNormalColor();
            }
            if (mouseAbove && backgroundHighlight) {
                bgColor = panel.getBackgroundHighlightColor();
            }
            return bgColor;
        };
        UIGetter<SVector> outlineColorGetter = () -> {
            SVector outlineColor = null;
            if (outlineNormal) {
                outlineColor = panel.getOutlineNormalColor();
            }
            if (mouseAbove && outlineHighlight) {
                outlineColor = panel.getOutlineHighlightColor();
            }
            return outlineColor;
        };
        UIGetter<Double> strokeWeightGetter = () -> panel.getStrokeWeight();

        style = new UIStyle(backgroundColorGetter, outlineColorGetter, strokeWeightGetter);
    }
}