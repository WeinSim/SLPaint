package sutil.ui;

import sutil.math.SVector;

public abstract class UIDragContainer<D extends Draggable> extends UIContainer {

    protected D draggable;

    protected boolean dragging = false;
    private SVector dragStartMouse;
    private SVector dragStartD;

    public UIDragContainer(D draggable) {
        super(0, 0);

        this.draggable = draggable;
        super.add(draggable);

        dragStartMouse = new SVector();
        dragStartD = new SVector();

        setLeftClickAction(this::startDragging);
    }

    @Override
    public void update() {
        super.update();

        if (!panel.isLeftMousePressed())
            dragging = false;

        if (dragging) {
            SVector newDragPos = new SVector(mousePosition).sub(position).sub(dragStartMouse).add(dragStartD);

            double relativeX = newDragPos.x / (size.x - draggable.size.x);
            if (!Double.isFinite(relativeX))
                relativeX = 0;
            draggable.setRelativeX(relativeX);

            double relativeY = newDragPos.y / (size.y - draggable.size.y);
            if (!Double.isFinite(relativeY))
                relativeY = 0;
            draggable.setRelativeY(relativeY);

            panel.setDragging();
        }
    }

    private void startDragging() {
        dragging = true;
        dragStartD.set(draggable.position);
        if (!draggable.mouseAbove) {
            dragStartD.set(draggable.size).scale(-0.5);
            dragStartD.add(mousePosition).sub(position);
        }
        dragStartMouse.set(mousePosition).sub(position);
    }

    @Override
    public void add(UIElement child) {
        // TODO: why can a UIDragContainer only contain one element?
        throw new UnsupportedOperationException("A UIDragContainer must contain exactly one element.");
    }

    public boolean isDragging() {
        return dragging;
    }
}