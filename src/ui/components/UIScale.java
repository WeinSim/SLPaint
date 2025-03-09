package ui.components;

import sutil.math.SVector;
import sutil.ui.Draggable;
import sutil.ui.UIDragContainer;
import sutil.ui.UIElement;
import sutil.ui.UIGetter;
import sutil.ui.UISetter;
import sutil.ui.UIStyle;
import ui.Colors;
import ui.Sizes;

/**
 * Formerly UIScaleContainer
 */
public class UIScale extends UIDragContainer<UIScale.Slider> {

    private static final UIStyle SLIDER_STYLE = new UIStyle(
            () -> Colors.getTextColor(),
            () -> null,
            () -> Sizes.STROKE_WEIGHT.size);

    public UIScale(int orientation, UIGetter<Double> getter, UISetter<Double> setter) {
        super(new Slider(orientation, getter, setter));

        setFillSize();
        noOutline();
    }

    public SVector getScaleOffset() {
        return orientation == VERTICAL
                ? new SVector(super.getMargin(), 0)
                : new SVector(0, super.getMargin());
    }

    @Override
    public double getMargin() {
        // return 2 * super.getMargin() + Sizes.UI_SCALE_MARGIN.size;
        return 0;
    }

    /**
     * The white visual indicators
     */
    protected static class Slider extends UIElement implements Draggable {

        private static final double WIDTH = 20;

        private int orientation;

        private UIGetter<Double> getter;
        private UISetter<Double> setter;

        public Slider(int orientation, UIGetter<Double> getter, UISetter<Double> setter) {
            this.orientation = orientation;

            this.getter = getter;
            this.setter = setter;

            setStyle(SLIDER_STYLE);
        }

        @Override
        public void setMinSize() {
            if (orientation == VERTICAL) {
                size.set(WIDTH, 2);
            } else {
                size.set(2, WIDTH);
            }
        }

        @Override
        public double getRelativeX() {
            return orientation == VERTICAL ? 0 : getter.get();
        }

        @Override
        public double getRelativeY() {
            return orientation == VERTICAL ? getter.get() : 0;
        }

        @Override
        public void setRelativeX(double x) {
            if (orientation == HORIZONTAL) {
                setter.set(Math.min(Math.max(0, x), 1));
            }
        }

        @Override
        public void setRelativeY(double y) {
            if (orientation == VERTICAL) {
                setter.set(Math.min(Math.max(0, y), 1));
            }
        }
    }
}