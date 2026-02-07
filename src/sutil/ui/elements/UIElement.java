package sutil.ui.elements;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;
import static org.lwjgl.glfw.GLFW.GLFW_POINTING_HAND_CURSOR;

import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

import org.lwjglx.util.vector.Vector4f;

import sutil.SUtil;
import sutil.math.SVector;
import sutil.ui.UI;
import sutil.ui.UIColors;
import sutil.ui.UIShape;
import sutil.ui.UISizes;
import sutil.ui.UIStyle;

public abstract class UIElement {

    protected SVector position;
    protected SVector size;
    protected UIContainer parent;
    protected int relativeLayer;

    protected boolean outlineNormal = false;
    protected boolean outlineHighlight = false;
    protected boolean backgroundNormal = false;
    protected boolean backgroundHighlight = false;
    protected UIStyle style;

    protected SVector mousePosition;
    protected boolean mouseAbove = false;

    protected boolean handCursorAbove = false;
    protected Supplier<Integer> cursorShapeSupplier = () -> handCursorAbove && mouseAbove
            ? GLFW_POINTING_HAND_CURSOR
            : null;

    protected boolean ignoreParentClipArea = false;

    protected Runnable leftClickAction = null, rightClickAction = null;
    protected boolean selectOnClick = false;
    protected boolean selectable = false;

    protected BooleanSupplier visibilitySupplier = this::isVisible;
    private boolean visible = true;

    public UIElement() {
        position = new SVector();
        size = new SVector();
        relativeLayer = 0;

        mousePosition = new SVector();

        setDefaultStyle();
    }

    public void updateVisibility() {
        visible = visibilitySupplier.getAsBoolean();

        if (visible && this == UI.getSelectedElement()) {
            UI.confirmSelectedElement();
        }
    }

    public void updateMousePosition(SVector mouse) {
        mousePosition.set(mouse);
    }

    public boolean updateMouseAbove(boolean valid, boolean insideParent, int currentLayer, final int targetLayer) {
        currentLayer += relativeLayer;
        if (currentLayer != targetLayer)
            return false;

        if (!valid || (!insideParent && !ignoreParentClipArea))
            mouseAbove = false;
        else
            mouseAbove = calculateMouseAbove(mousePosition);

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

        // if ((mods & (GLFW_MOD_SHIFT | GLFW_MOD_CONTROL)) != 0)
        // return;

        switch (mouseButton) {
            case GLFW_MOUSE_BUTTON_LEFT -> {
                if (leftClickAction != null) {
                    leftClickAction.run();
                }

                if (selectOnClick) {
                    UI.select(this, mousePosition);
                }
            }
            case GLFW_MOUSE_BUTTON_RIGHT -> {
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
     * @param mods     Only contains the {@code GLFW_MOD_CONTROL} and
     *                 {@code GLFW_MOD_SHIFT} modifiers
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

    public boolean ignoreParentClipArea() {
        return ignoreParentClipArea;
    }

    public final boolean isVisible() {
        return visible;
    }

    public UIElement setVisibilitySupplier(BooleanSupplier visibilitySupplier) {
        this.visibilitySupplier = visibilitySupplier;
        return this;
    }

    public boolean mouseAbove() {
        return mouseAbove;
    }

    public void setLeftClickAction(Runnable leftClickAction) {
        this.leftClickAction = leftClickAction;
    }

    public Runnable getLeftClickAction() {
        return leftClickAction;
    }

    public void setRightClickAction(Runnable rightClickAction) {
        this.rightClickAction = rightClickAction;
    }

    public Runnable getRightClickAction() {
        return rightClickAction;
    }

    public SVector getAbsolutePosition() {
        return parent.getAbsolutePosition().add(position);
    }

    public UIElement setHandCursor() {
        handCursorAbove = true;
        return this;
    }

    public void setCursorShape(Integer cursorShape) {
        setCursorShape(() -> cursorShape);
    }

    public void setCursorShape(Supplier<Integer> cursorShapeSupplier) {
        this.cursorShapeSupplier = cursorShapeSupplier;
    }

    public Integer getCursorShape() {
        return cursorShapeSupplier.get();
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
        if (ol == null && UI.getSelectedElement() == this) {
            ol = UIColors.OUTLINE.get();
        }
        return ol;
    }

    public final double strokeWeight() {
        double sw = style.strokeWeight();
        if (UI.getSelectedElement() == this) {
            if (style.strokeColor() == null) {
                sw = UISizes.STROKE_WEIGHT.get();
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

    public UIShape shape() {
        return style.shape();
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
            if (backgroundNormal)
                bgColor = UIColors.BACKGROUND.get();
            if (backgroundHighlight && mouseAbove)
                bgColor = UIColors.BACKGROUND_HIGHLIGHT.get();
            return bgColor;
        };
        Supplier<Vector4f> outlineColorSupplier = () -> {
            Vector4f outlineColor = null;
            if (outlineNormal || (outlineHighlight && mouseAbove))
                outlineColor = UIColors.OUTLINE.get();
            return outlineColor;
        };
        DoubleSupplier strokeWeightSupplier = UISizes.STROKE_WEIGHT;

        style = new UIStyle(backgroundColorSupplier, outlineColorSupplier, strokeWeightSupplier);
    }
}