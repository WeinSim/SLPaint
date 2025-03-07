package sutil.ui;

import sutil.math.SVector;

public class UIScrollArea extends UIContainer {

    public static final int NONE = 2, BOTH = 3;

    private SVector scrollOffset;
    private SVector areaOvershoot;
    private int scrollMode;

    private UIScrollBarContainer hScrollBarContainer = null;
    private UIScrollBarContainer vScrollBarContainer = null;

    public UIScrollArea(int orientation, int alignment, int scrollMode) {
        super(orientation, alignment);
        this.scrollMode = scrollMode;

        setMaximalSize();

        scrollOffset = new SVector();
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
        areaOvershoot = new SVector(
                Math.max(0, boundingBox.x - size.x),
                Math.max(0, boundingBox.y - size.y));

        scrollOffset.x = Math.min(0, Math.max(scrollOffset.x, -areaOvershoot.x));
        scrollOffset.y = Math.min(0, Math.max(scrollOffset.y, -areaOvershoot.y));
    }

    @Override
    public void positionChildren() {
        super.positionChildren();

        for (UIElement child : children) {
            child.getPosition().add(scrollOffset);
        }
    }

    @Override
    public void mouseWheel(SVector scroll, SVector mousePos) {
        super.mouseWheel(scroll, mousePos);

        scrollOffset.x += isHScroll() ? scroll.x : 0;
        scrollOffset.y += isVScroll() ? scroll.y : 0;
    }

    public void setScrollMode(int scrollMode) {
        if (scrollMode < 0 || scrollMode >= 4) {
            throw new IllegalArgumentException(String.format("Invalid scrollMode (%d)", scrollMode));
        }
        this.scrollMode = scrollMode;
    }

    public UIContainer addScrollBars() {
        UIContainer ret = this;
        if (isHScroll()) {
            ret = wrapHScroll(this);
        }
        if (isVScroll()) {
            ret = wrapVScroll(ret);
        }
        return ret;
    }

    public void setRelativeScrollX(double scrollX) {
        scrollOffset.x = -areaOvershoot.x * Math.min(Math.max(0, scrollX), 1);
    }

    public void setRelativeScrollY(double scrollY) {
        scrollOffset.y = -areaOvershoot.y * Math.min(Math.max(0, scrollY), 1);
    }

    public double getRelativeScrollX() {
        return areaOvershoot.x <= 0.0 ? 0 : -scrollOffset.x / areaOvershoot.x;
    }

    public double getRelativeScrollY() {
        return areaOvershoot.y <= 0.0 ? 0 : -scrollOffset.y / areaOvershoot.y;
    }

    private double getWidthFraction() {
        return size.x / (size.x + areaOvershoot.x);
    }

    private double getHeightFraction() {
        return size.y / (size.y + areaOvershoot.y);
    }

    private boolean isHScroll() {
        return switch (scrollMode) {
            case HORIZONTAL, BOTH -> true;
            default -> false;
        };
    }

    private boolean isVScroll() {
        return switch (scrollMode) {
            case VERTICAL, BOTH -> true;
            default -> false;
        };
    }

    private UIContainer wrapHScroll(UIContainer container) {
        UIContainer wrapper = new UIContainer(UIContainer.VERTICAL, 0);
        wrapper.noBackground().noOutline();
        wrapper.zeroMargin().zeroPadding().setMinimalSize();

        wrapper.add(container);
        hScrollBarContainer = new UIScrollBarContainer(this, HORIZONTAL);
        wrapper.add(hScrollBarContainer);
        return wrapper;
    }

    private UIContainer wrapVScroll(UIContainer container) {
        UIContainer wrapper = new UIContainer(UIContainer.HORIZONTAL, 0);
        wrapper.noBackground().noOutline();
        wrapper.zeroMargin().zeroPadding().setMinimalSize();

        wrapper.add(container);
        vScrollBarContainer = new UIScrollBarContainer(this, VERTICAL);
        wrapper.add(vScrollBarContainer);
        return wrapper;
    }

    private class UIScrollBarContainer extends UIDragContainer<UIScrollBar> {

        UIScrollBarContainer(UIScrollArea scrollArea, int orientation) {
            super(new UIScrollBar(scrollArea, orientation));

            withOutline();
            withBackground();
            setFillSize();
            zeroMargin();
        }

        @Override
        public void expandAsNeccessary(SVector remainingSize) {
            super.expandAsNeccessary(remainingSize);

            draggable.expandAsNeccessary();
        }
    }

    private class UIScrollBar extends Draggable {

        private UIScrollArea scrollArea;
        private int orientation;

        UIScrollBar(UIScrollArea scrollArea, int orientation) {
            this.scrollArea = scrollArea;
            this.orientation = orientation;

            UIStyle style = new UIStyle(
                    () -> mouseAbove || isBeingDragged()
                            ? panel.getOutlineNormalColor()
                            : panel.getOutlineHighlightColor(),
                    () -> null,
                    () -> 0.0);
            setStyle(style);
        }

        @Override
        public void setMinSize() {
            double min = 2 * getMargin();
            size.set(min, min);
        }

        public void expandAsNeccessary() {
            if (orientation == VERTICAL) {
                size.y = scrollArea.getHeightFraction() * parent.size.y;
            } else {
                size.x = scrollArea.getWidthFraction() * parent.size.x;
            }
        }

        @Override
        public double getRelativeX() {
            return orientation == VERTICAL ? 0 : scrollArea.getRelativeScrollX();
        }

        @Override
        public double getRelativeY() {
            return orientation == VERTICAL ? scrollArea.getRelativeScrollY() : 0;
        }

        @Override
        public void setRelativeX(double x) {
            if (orientation == HORIZONTAL) {
                scrollArea.setRelativeScrollX(x);
            }
        }

        @Override
        public void setRelativeY(double y) {
            if (orientation == VERTICAL) {
                scrollArea.setRelativeScrollY(y);
            }
        }
    }
}