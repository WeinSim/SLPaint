package ui.components;

import main.ColorPicker;
import renderEngine.Window;
import sutil.math.SVector;
import sutil.ui.UIContainer;
import ui.Sizes;

public abstract class UIScale extends UIContainer implements DragTarget {

    private UIScaleContainer parent;

    protected ColorPicker colorPicker;

    public UIScale(int orientation, ColorPicker colorPicker) {
        super(orientation, 0);
        this.colorPicker = colorPicker;

        setFillSize();
        noOutline();
        setClickAction(() -> colorPicker.setDragTarget(this));
    }

    @Override
    public void drag() {
        Window window = colorPicker.getWindow();
        SVector mousePos = window.getMousePosition();
        SVector absolutePos = getAbsolutePosition();
        mousePos = new SVector(mousePos).sub(absolutePos);
        if (orientation == VERTICAL) {
            mousePos.y = Math.min(Math.max(0, mousePos.y), size.y);
        } else {
            mousePos.x = Math.min(Math.max(0, mousePos.x), size.x);
        }
        setColorDimension(mousePos);
    }

    public abstract void setColorDimension(SVector mousePos);

    @Override
    public void updateCursorPosition() {
        parent.setSliderPosition(getSliderCoord());
    }

    protected abstract double getSliderCoord();

    @Override
    public double getMargin() {
        return Sizes.UI_SCALE_MARGIN.size;
    }

    public void setParent(UIScaleContainer parent) {
        this.parent = parent;
    }
}