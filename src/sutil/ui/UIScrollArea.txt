package sutil.ui;

import sutil.math.SVector;

public class UIScrollArea extends UIContainer {

    protected double scrollOffset;
    protected double areaOvershoot;

    public UIScrollArea(int orientation, int alignment) {
        super(orientation, alignment);

        setMaximalSize();

        scrollOffset = 0;
    }

    @Override
    public void update(SVector mouse) {
        updateMouseAboveReference(mouse);

        SVector relativeMouse = mouseAbove ? new SVector(mouse).sub(position) : null;
        for (UIElement child : children) {
            child.update(relativeMouse);
        }
    }

    @Override
    public void expandAsNeccessary(SVector remainingSize) {
        super.expandAsNeccessary(remainingSize);

        SVector boundingBox = getChildrenBoundingBox(getMargin(), getPadding(), true);
        areaOvershoot = switch (orientation) {
            case VERTICAL -> boundingBox.y - size.y;
            case HORIZONTAL -> boundingBox.x - size.x;
            default -> 0;
        };
        areaOvershoot = Math.max(0, areaOvershoot);

        scrollOffset = Math.min(0, Math.max(scrollOffset, -areaOvershoot));
    }

    @Override
    public void positionChildren() {
        super.positionChildren();

        for (UIElement child : children) {
            switch (orientation) {
                case VERTICAL -> child.getPosition().y += scrollOffset;
                case HORIZONTAL -> child.getPosition().x += scrollOffset;
            }
        }
    }

    @Override
    public void mouseWheel(double scroll, SVector mousePos) {
        super.mouseWheel(scroll, mousePos);

        scrollOffset += scroll;
    }
}