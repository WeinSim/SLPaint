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

        setLeftClickAction(() -> {
            dragging = true;
            dragStartD.set(draggable.position);
            if (!draggable.mouseAbove) {
                dragStartD.set(draggable.size).scale(-0.5);
                dragStartD.add(mousePosition).sub(position);
                clampDraggablePosition(dragStartD);
            }
            dragStartMouse.set(mousePosition).sub(position);
        });
    }

    @Override
    public void update() {
        super.update();

        if (!panel.isMousePressed()) {
            dragging = false;
        }
        if (dragging) {
            drag(mousePosition);
        }
    }

    protected void drag(SVector mouse) {
        if (mouse == null) {
            return;
        }
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
    public void mousePressed(int mouseButton) {
        super.mousePressed(mouseButton);

        if (mouseAbove() && mouseButton == LEFT) {
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