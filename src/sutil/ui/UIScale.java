package sutil.ui;

import java.util.function.Consumer;
import java.util.function.Supplier;

import sutil.math.SVector;

public class UIScale extends UIDragContainer {

    private Supplier<Double> getter;
    private Consumer<Double> setter;

    private boolean narrow;

    public UIScale(int orientation, Supplier<Double> getter, Consumer<Double> setter) {
        this(orientation, getter, setter, true);
    }

    public UIScale(int orientation, Supplier<Double> getter, Consumer<Double> setter, boolean narrow) {
        this.orientation = orientation;

        this.getter = getter;
        this.setter = setter;
        this.narrow = narrow;

        setAlignment(CENTER);

        noOutline();
        zeroMargin();
        zeroPadding();

        if (orientation == VERTICAL) {
            setHMinimalSize();
            setVFillSize();
        } else {
            setHFillSize();
            setVMinimalSize();
        }

        add(new Filler());
        add(getVisuals(orientation));
        add(new Slider(orientation));
    }

    protected Visuals getVisuals(int orientation) {
        return new Visuals(orientation);
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

    private double getVisualWidth() {
        return narrow ? UISizes.SCALE_NARROW.get() : UISizes.SCALE_WIDE.get();
    }

    /**
     * The bar in the the middle
     */
    protected class Visuals extends UIContainer {

        public Visuals(int orientation) {
            super(orientation, 0);

            style.setBackgroundColor(UIColors.OUTLINE_NORMAL);

            noOutline();
            zeroMargin();

            double s = getVisualWidth();
            if (orientation == VERTICAL) {
                setHFixedSize(s);
                setVFillSize();
            } else {
                setHFillSize();
                setVFixedSize(s);
            }
        }
    }

    /**
     * The white visual indicators
     */
    protected class Slider extends UIFloatContainer {

        public Slider(int orientation) {
            super(orientation, 0);

            setStyle(new UIStyle(UIColors.TEXT, () -> null, UISizes.STROKE_WEIGHT));

            addAnchor(
                    orientation == VERTICAL ? Anchor.CENTER_LEFT : Anchor.TOP_CENTER,
                    () -> {
                        return new SVector(getRelativeX(), getRelativeY()).mult(parent.getSize());
                    });

            double len = 2 * UISizes.SCALE_SLIDER_LENGTH.get() + getVisualWidth();
            double width = UISizes.SCALE_SLIDER_WIDTH.get();
            if (orientation == VERTICAL) {
                setFixedSize(new SVector(len, width));
            } else {
                setFixedSize(new SVector(width, len));
            }

            relativeLayer = 0;
            clipToRoot = false;
            ignoreClipArea = false;
        }
    }

    /**
     * Responsible for the gaps around the Visuals
     */
    private class Filler extends UIElement {

        Filler() {
        }

        @Override
        public void setPreferredSize() {
            double w = 2 * UISizes.SCALE_SLIDER_LENGTH.get() + getVisualWidth();

            if (orientation == VERTICAL) {
                size.set(w, 0);
            } else {
                size.set(0, w);
            }
        }
    }
}