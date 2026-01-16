package sutil.ui;

import sutil.math.SVector;

public abstract class Draggable extends UIFloatContainer {

    public Draggable() {
        this(0, 0);
    }

    public Draggable(int orientation, int alignment) {
        super(orientation, alignment);
    }

    @Override
    public void update() {
        super.update();

        clearAnchors();

        SVector pos = new SVector(getRelativeX(), getRelativeY());
        pos.x *= parent.size.x - size.x;
        pos.y *= parent.size.y - size.y;

        addAnchor(Anchor.TOP_LEFT, pos);
    }

    /**
     * @return The underlying value represented by the x-coordinate of this
     *         {@code Draggable}, in the range from 0 (minimum value) to 1 (maximum
     *         value).
     */
    public abstract double getRelativeX();

    /**
     * @return The underlying value represented by the y-coordinate of this
     *         {@code Draggable}, in the range from 0 (minimum value) to 1 (maximum
     *         value).
     */
    public abstract double getRelativeY();

    /**
     * Sets the underlying value represented by the x-coordinate of this
     * {@code Draggable}.
     * 
     * @param x The new value, which is <b>not</b> guaranteed to be in the range
     *          from 0 to 1.
     */
    public abstract void setRelativeX(double x);

    /**
     * Sets the underlying value represented by the y-coordinate of this
     * {@code Draggable}.
     * 
     * @param y The new value, which is <b>not</b> guaranteed to be in the range
     *          from 0 to 1.
     */
    public abstract void setRelativeY(double y);
}