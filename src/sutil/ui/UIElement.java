package sutil.ui;

import java.util.function.Supplier;

import org.lwjgl.glfw.GLFW;
import org.lwjglx.util.vector.Vector4f;

import sutil.SUtil;
import sutil.math.SVector;

public abstract class UIElement {

    protected SVector position;
    protected SVector size;
    protected UIContainer parent;
    protected int relativeLayer;

    protected UIPanel panel;

    protected boolean outlineNormal = false;
    protected boolean outlineHighlight = false;
    protected boolean backgroundNormal = false;
    protected boolean backgroundHighlight = false;
    protected UIStyle style;

    protected SVector mousePosition;
    protected boolean mouseAbove = false;

    protected UIAction leftClickAction = null,
            rightClickAction = null;
    protected boolean selectOnClick = false;
    protected boolean selectable = false;

    protected Supplier<Boolean> visibilitySupplier = this::isVisible;
    private boolean visible = true;

    public UIElement() {
        position = new SVector();
        size = new SVector();
        relativeLayer = 0;

        mousePosition = new SVector();

        setDefaultStyle();
    }

    public void updateVisibility() {
        visible = visibilitySupplier.get();

        if (this == panel.getSelectedElement()) {
            // TODO: shouldn't this only happen when visible == true?
            panel.confirmSelectedElement();
        }
    }

    public void updateMousePosition(SVector mouse) {
        mousePosition.set(mouse);
    }

    public boolean updateMouseAbove(boolean valid, boolean insideParent, int currentLayer, final int targetLayer) {
        currentLayer += relativeLayer;
        if (currentLayer != targetLayer)
            return false;

        mouseAbove = valid && insideParent ? calculateMouseAbove(mousePosition) : false;

        return mouseAbove;
    }

    protected boolean calculateMouseAbove(SVector mouse) {
        return SUtil.pointInsideRect(mouse, position, size);
    }

    public void update() {
    }

    public void mousePressed(int mouseButton, int mods) {
        if (!mouseAbove)
            return;

        if ((mods & (GLFW.GLFW_MOD_SHIFT | GLFW.GLFW_MOD_CONTROL)) != 0)
            return;

        switch (mouseButton) {
            case UIPanel.LEFT -> {
                if (leftClickAction != null) {
                    leftClickAction.run();
                }

                if (selectOnClick) {
                    panel.select(this, mousePosition);
                }
            }
            case UIPanel.RIGHT -> {
                if (rightClickAction != null) {
                    rightClickAction.run();
                }
            }
        }
    }

    public void mouseReleased(int mouseButton, int mods) {

    }

    /**
     * @param scroll
     * @param mousePos
     * @param mods     Only contains the {@code GLFW.GLFW_MOD_CONTROL} and
     *                 {@code GLFW.GLFW_MOD_SHIFT} modifiers
     * @return Wether the mouse scroll action has been "used up" by this
     *         {@code UIElement}.
     */
    public boolean mouseWheel(SVector scroll, SVector mousePos, int mods) {
        return false;
    }

    public void keyPressed(int key, int mods) {
    }

    public void charInput(char c) {
    }

    public abstract void setPreferredSize();

    /**
     * 
     * @param mouse the relative mouse position of the mouse press that caused this
     *              element to be selected, or {@code null} if this selection didn't
     *              come from a mouse press
     */
    public void select(SVector mouse) {
    }

    public boolean isSelectable() {
        return selectable;
    }

    public void setSelectable(boolean selectable) {
        this.selectable = selectable;
    }

    public SVector getPosition() {
        return position;
    }

    public SVector getSize() {
        return size;
    }

    public int getRelativeLayer() {
        return relativeLayer;
    }

    public UIElement setRelativeLayer(int relativeLayer) {
        this.relativeLayer = relativeLayer;
        return this;
    }

    public final boolean isVisible() {
        return visible;
    }

    public UIElement setVisibilitySupplier(Supplier<Boolean> visibilitySupplier) {
        this.visibilitySupplier = visibilitySupplier;
        return this;
    }

    protected void setPanel(UIPanel panel) {
        this.panel = panel;
    }

    public boolean mouseAbove() {
        return mouseAbove;
    }

    public void setLeftClickAction(UIAction leftClickAction) {
        this.leftClickAction = leftClickAction;
    }

    public UIAction getLeftClickAction() {
        return leftClickAction;
    }

    public void setRightClickAction(UIAction rightClickAction) {
        this.rightClickAction = rightClickAction;
    }

    public UIAction getRightClickAction() {
        return rightClickAction;
    }

    public SVector getAbsolutePosition() {
        return parent.getAbsolutePosition().add(position);
    }

    public UIPanel getPanel() {
        return panel;
    }

    public Vector4f backgroundColor() {
        return style.backgroundColor();
    }

    public boolean doBackgroundCheckerboard() {
        return style.doBackgroundCheckerboard();
    }

    public Vector4f backgroundCheckerboardColor1() {
        return style.backgroundCheckerboardColor1();
    }

    public Vector4f backgroundCheckerboardColor2() {
        return style.backgroundCheckerboardColor2();
    }

    public double backgroundCheckerboardSize() {
        return style.backgroundCheckerboardSize();
    }

    public final Vector4f strokeColor() {
        Vector4f ol = style.strokeColor();
        if (ol == null && panel.getSelectedElement() == this) {
            ol = panel.get(UIColors.OUTLINE_NORMAL);
        }
        return ol;
    }

    public final double strokeWeight() {
        double sw = style.strokeWeight();
        if (panel.getSelectedElement() == this) {
            if (style.strokeColor() == null) {
                sw = panel.get(UISizes.STROKE_WEIGHT);
            } else {
                sw *= 2;
            }
        }
        return sw;
    }

    public boolean doStrokeCheckerboard() {
        return style.doStrokeCheckerboard();
    }

    public Vector4f strokeCheckerboardColor1() {
        return style.strokeCheckerboardColor1();
    }

    public Vector4f strokeCheckerboardColor2() {
        return style.strokeCheckerboardColor2();
    }

    public double strokeCheckerboardSize() {
        return style.strokeCheckerboardSize();
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
        Supplier<Vector4f> backgroundColorSupplier = () -> {
            Vector4f bgColor = null;
            if (backgroundNormal) {
                bgColor = panel.get(UIColors.BACKGROUND_NORMAL);
            }
            if (mouseAbove && backgroundHighlight) {
                bgColor = panel.get(UIColors.BACKGROUND_HIGHLIGHT);
            }
            return bgColor;
        };
        Supplier<Vector4f> outlineColorSupplier = () -> {
            Vector4f outlineColor = null;
            if (outlineNormal) {
                outlineColor = panel.get(UIColors.OUTLINE_NORMAL);
            }
            if (mouseAbove && outlineHighlight) {
                outlineColor = panel.get(UIColors.OUTLINE_HIGHLIGHT);
            }
            return outlineColor;
        };
        Supplier<Double> strokeWeightSupplier = () -> panel.get(UISizes.STROKE_WEIGHT);

        style = new UIStyle(backgroundColorSupplier, outlineColorSupplier, strokeWeightSupplier);
    }
}