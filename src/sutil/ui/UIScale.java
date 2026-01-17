package sutil.ui;

import java.util.function.Consumer;
import java.util.function.Supplier;

import sutil.math.SVector;

public class UIScale extends UIDragContainer {

    private Supplier<Double> getter;
    private Consumer<Double> setter;

    protected boolean narrow;

    public UIScale(int orientation, Supplier<Double> getter, Consumer<Double> setter) {
        this.orientation = orientation;

        this.getter = getter;
        this.setter = setter;

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

        narrow = true;
    }

    protected Visuals getVisuals(int orientation) {
        return new Visuals(orientation);
    }

    @Override
    public void update() {
        super.update();
    }

    // protected double getScaleWidth() {
    //     double margin = orientation == VERTICAL ? getHMargin() : getVMargin();
    //     return narrow ? 2 : 2 * margin;
    // }

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
        return panel.get(narrow ? UISizes.SCALE_NARROW : UISizes.SCALE_WIDE);
    }

    /**
     * The bar in the the middle
     */
    protected class Visuals extends UIContainer {

        public Visuals(int orientation) {
            super(orientation, 0);

            style.setBackgroundColor(() -> panel.get(UIColors.OUTLINE_NORMAL));

            noOutline();
            zeroMargin();
        }

        @Override
        public void update() {
            super.update();

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

            setStyle(new UIStyle(() -> panel.get(UIColors.TEXT), () -> null, () -> panel.get(UISizes.STROKE_WEIGHT)));

            relativeLayer = 0;
            clipToRoot = false;
            ignoreClipArea = false;
        }

        @Override
        public void update() {
            super.update();

            double len = 2 * panel.get(UISizes.SCALE_SLIDER_LENGTH) + getVisualWidth();
            double width = panel.get(UISizes.SCALE_SLIDER_WIDTH);

            if (orientation == VERTICAL) {
                setFixedSize(new SVector(len, width));
            } else {
                setFixedSize(new SVector(width, len));
            }

            clearAnchors();
            SVector pos = new SVector(getRelativeX(), getRelativeY()).mult(parent.getSize());
            addAnchor(orientation == VERTICAL ? Anchor.CENTER_LEFT : Anchor.TOP_CENTER, pos);
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
            double w = 2 * panel.get(UISizes.SCALE_SLIDER_LENGTH) + getVisualWidth();

            if (orientation == VERTICAL) {
                size.set(w, 0);
            } else {
                size.set(0, w);
            }
        }
    }
}