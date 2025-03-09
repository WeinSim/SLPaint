package sutil.ui;

/**
 * A {@code UIElement} that can be dragged around with the mouse must implement
 * the {@code Draggable} interface. Such a {@code UIElement} always represents
 * some underlying value (e.g. volume / brightness / transparency), which can be
 * either one- or two-dimensional. The {@code Draggable} interface is used for
 * communicating these underlying values.
 */
public interface Draggable {

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