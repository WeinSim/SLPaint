package sutil.ui;

import sutil.math.SVector;

public abstract class UIDragContainer<D extends UIElement & Draggable> extends UIContainer {

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

        if (!panel.isLeftMousePressed()) {
            dragging = false;
        }
        if (dragging) {
            drag(mousePosition);
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

    protected void drag(SVector mouse) {
        if (mouse == null) {
            return;
        }
        SVector newDragPos = new SVector(mouse).sub(position).sub(dragStartMouse).add(dragStartD);

        double relativeX = newDragPos.x / (size.x - draggable.size.x);
        if (!Double.isFinite(relativeX)) {
            relativeX = 0;
        }
        draggable.setRelativeX(relativeX);

        double relativeY = newDragPos.y / (size.y - draggable.size.y);
        if (!Double.isFinite(relativeY)) {
            relativeY = 0;
        }
        draggable.setRelativeY(relativeY);
    }

    @Override
    public void add(UIElement child) {
        throw new UnsupportedOperationException("A UIDragContainer must contain exactly one element.");
    }

    @Override
    public void positionChildren() {
        SVector relativePos = new SVector(draggable.getRelativeX(), draggable.getRelativeY());
        relativePos.x *= size.x - draggable.size.x;
        relativePos.y *= size.y - draggable.size.y;
        draggable.position.set(relativePos);
    }

    public boolean isDragging() {
        return dragging;
    }
}