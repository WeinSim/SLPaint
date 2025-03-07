package sutil.ui;

/**
 * A {@code Draggable} is a {@code UIElement} that can be dragged around with
 * the mouse. Every {@code Draggable} represents some underlying value (e.g.
 * volume / brightness / transparency), which can be either one- or
 * two-dimensional.
 */
public abstract class Draggable extends UIElement {

    private UIDragContainer<?> dragContainer;

    public void setDragContainer(UIDragContainer<?> dragContainer) {
        this.dragContainer = dragContainer;
    }

    protected boolean isBeingDragged() {
        return dragContainer.isDragging();
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
     * @param x The new value, in the range from 0 (minimum value) to 1 (maximum
     *          value).
     */
    public abstract void setRelativeX(double x);

    /**
     * Sets the underlying value represented by the y-coordinate of this
     * {@code Draggable}.
     * 
     * @param y The new value, in the range from 0 (minimum value) to 1 (maximum
     *          value).
     */
    public abstract void setRelativeY(double y);
}