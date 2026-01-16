package sutil.ui;

import java.util.function.Consumer;
import java.util.function.Supplier;

import sutil.math.SVector;
import ui.AppUI;
import ui.Sizes;

public class UIScale extends UIDragContainer<UIScale.Slider> {

    protected boolean narrow;

    public UIScale(int orientation, Supplier<Double> getter, Consumer<Double> setter) {
        super(new Slider(orientation, getter, setter));
        this.orientation = orientation;

        noOutline();
        if (orientation == VERTICAL) {
            setHMinimalSize();
            setVFillSize();
        } else {
            setHFillSize();
            setVMinimalSize();
        }

        narrow = true;
    }

    // @Override
    // public void update() {
    // super.update();

    // if (orientation == VERTICAL) {
    // setHFixedSize(2 * getHMargin() + getScaleWidth());
    // } else {
    // setVFixedSize(2 * getVMargin() + getScaleWidth());
    // }
    // }

    // public SVector getScaleSize() {
    // return getScaleOffset().scale(-2).add(size);
    // }

    // public SVector getScaleOffset() {
    // return orientation == VERTICAL ? new SVector(getHMargin(), 0) : new
    // SVector(0, getVMargin());
    // }

    protected double getScaleWidth() {
        double margin = orientation == VERTICAL ? getHMargin() : getVMargin();
        return narrow ? 2 : 2 * margin;
    }

    protected static class Visuals extends UIElement {

        @Override
        public void setPreferredSize() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'setPreferredSize'");
        }

    }

    /**
     * The white visual indicators
     */
    protected static class Slider extends Draggable {

        private Supplier<Double> getter;
        private Consumer<Double> setter;

        public Slider(int orientation, Supplier<Double> Supplier, Consumer<Double> setter) {
            super(orientation, 0);

            this.getter = Supplier;
            this.setter = setter;

            setStyle(new UIStyle(
                    () -> panel.defaultTextColor(),
                    () -> null,
                    () -> panel.strokeWeightSize()));
        }

        @Override
        public void update() {
            super.update();

            double len = ((AppUI<?>) panel).getSize(Sizes.SCALE_SLIDER);
            double width = ((AppUI<?>) panel).getSize(Sizes.SCALE_NARROW);

            if (orientation == VERTICAL) {
                setFixedSize(new SVector(len, width));
            } else {
                setFixedSize(new SVector(width, len));
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