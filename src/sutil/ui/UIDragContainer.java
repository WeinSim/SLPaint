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
    }

    @Override
    public void update(SVector mouse) {
        super.update(mouse);

        if (!panel.isMousePressed()) {
            dragging = false;
        }
        if (dragging) {
            drag(mouse);
        }
    }

    protected void drag(SVector mouse) {
        SVector newDragPos = new SVector(mouse).sub(position).sub(dragStartMouse).add(dragStartD);
        // clampDraggablePosition(newDragPos);

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
    public void mousePressed(SVector mouse) {
        super.mousePressed(mouse);

        if (mouseAbove) {
            dragging = true;
            dragStartD.set(draggable.position);
            if (!draggable.mouseAbove) {
                dragStartD.set(draggable.size).scale(-0.5);
                dragStartD.add(mouse).sub(position);
                clampDraggablePosition(dragStartD);
            }
            dragStartMouse.set(mouse).sub(position);
        }
    }

    private void clampDraggablePosition(SVector newDragPos) {
        newDragPos.x = Math.min(Math.max(0, newDragPos.x), size.x - draggable.size.x);
        newDragPos.y = Math.min(Math.max(0, newDragPos.y), size.y - draggable.size.y);
    }

    @Override
    public void add(UIElement child) {
        throw new UnsupportedOperationException("A DragContainer must contain exactly one element.");
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