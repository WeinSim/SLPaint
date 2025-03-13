package ui.components;

import java.util.function.Consumer;
import java.util.function.Supplier;

import sutil.math.SVector;
import sutil.ui.Draggable;
import sutil.ui.UIDragContainer;
import sutil.ui.UIElement;
import sutil.ui.UIStyle;
import ui.Colors;
import ui.Sizes;

public class UIScale extends UIDragContainer<UIScale.Slider> {

    private static final UIStyle SLIDER_STYLE = new UIStyle(
            () -> Colors.getTextColor(),
            () -> null,
            () -> Sizes.STROKE_WEIGHT.size);

    public UIScale(int orientation, Supplier<Double> getter, Consumer<Double> setter) {
        super(new Slider(orientation, getter, setter));
        this.orientation = orientation;

        noOutline();
    }

    public SVector getScaleSize() {
        return getScaleOffset().scale(-2).add(size);
    }

    public SVector getScaleOffset() {
        return orientation == VERTICAL
                ? new SVector(getHMargin(), 0)
                : new SVector(0, getVMargin());
    }

    @Override
    public void setMinSize() {
        super.setMinSize();

        if (orientation == VERTICAL) {
            size.set(2 * getHMargin() + getScaleWidth(), 0);
        } else {
            size.set(0, 2 * getVMargin() + getScaleWidth());
        }
    }

    @Override
    public void expandAsNeccessary(SVector remainingSize) {
        super.expandAsNeccessary(remainingSize);

        draggable.expandAsNeccessary();
    }

    protected double getScaleWidth() {
        return 2 * (orientation == VERTICAL ? super.getHMargin() : super.getVMargin());
    }

    /**
     * The white visual indicators
     */
    protected static class Slider extends UIElement implements Draggable {

        private int orientation;

        private Supplier<Double> getter;
        private Consumer<Double> setter;

        public Slider(int orientation, Supplier<Double> getter, Consumer<Double> setter) {
            this.orientation = orientation;

            this.getter = getter;
            this.setter = setter;

            setStyle(SLIDER_STYLE);
        }

        @Override
        public void setMinSize() {
            size.set(1, 1);
        }

        public void expandAsNeccessary() {
            double width = orientation == VERTICAL
                    ? parent.getSize().x
                    : parent.getSize().y;

            if (orientation == VERTICAL) {
                size.set(width, 2 * getStrokeWeight());
            } else {
                size.set(2 * getStrokeWeight(), width);
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
                setter.accept(Math.min(Math.max(0, x), 1));
            }
        }

        @Override
        public void setRelativeY(double y) {
            if (orientation == VERTICAL) {
                setter.accept(Math.min(Math.max(0, y), 1));
            }
        }
    }
}