package sutil.ui;

import sutil.math.SVector;

public class UIScrollArea extends UIContainer {

    public static final int NONE = 2, BOTH = 3;

    private SVector scrollOffset;
    private SVector areaOvershoot;
    private int scrollMode;

    /**
     * Indicates wether a scroll bar should be hidden if there is not enough content
     * to allow scrolling.
     */
    private boolean hideScrollbars;

    public UIScrollArea(int orientation, int alignment, int scrollMode) {
        super(orientation, alignment);
        this.scrollMode = scrollMode;

        scrollOffset = new SVector();
        hideScrollbars = true;
    }

    @Override
    public void update(SVector mouse) {
        updateMouseAboveReference(mouse);

        SVector relativeMouse = mouseAbove ? new SVector(mouse).sub(position) : null;
        for (UIElement child : getChildren()) {
            child.update(relativeMouse);
        }
    }

    @Override
    protected SVector getRemainingSize() {
        double hMargin = getHMargin(), vMargin = getVMargin();
        double padding = getPadding();
        SVector remainingSize = getChildrenBoundingBox(hMargin, vMargin, padding, false);
        switch (orientation) {
            case VERTICAL -> remainingSize.x -= 2 * hMargin;
            case HORIZONTAL -> remainingSize.y -= 2 * vMargin;
        }
        // SVector superRemainingSize = super.getRemainingSize();
        // return new SVector(
        // Math.max(remainingSize.x, superRemainingSize.x),
        // Math.max(remainingSize.y, superRemainingSize.y));
        return remainingSize;
    }

    @Override
    public void expandAsNeccessary(SVector remainingSize) {
        super.expandAsNeccessary(remainingSize);

        SVector boundingBox = getChildrenBoundingBox(getHMargin(), getVMargin(), getPadding(), true);
        areaOvershoot = new SVector(
                Math.max(0, boundingBox.x - size.x),
                Math.max(0, boundingBox.y - size.y));

        scrollOffset.x = Math.min(0, Math.max(scrollOffset.x, -areaOvershoot.x));
        scrollOffset.y = Math.min(0, Math.max(scrollOffset.y, -areaOvershoot.y));
    }

    @Override
    public void positionChildren() {
        super.positionChildren();

        for (UIElement child : getChildren()) {
            child.getPosition().add(scrollOffset);
        }
    }

    @Override
    public void mouseWheel(SVector scroll, SVector mousePos) {
        super.mouseWheel(scroll, mousePos);

        scrollOffset.x += isHScroll() ? scroll.x : 0;
        scrollOffset.y += isVScroll() ? scroll.y : 0;
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

    private void setRelativeScrollX(double scrollX) {
        scrollOffset.x = -areaOvershoot.x * Math.min(Math.max(0, scrollX), 1);
    }

    private void setRelativeScrollY(double scrollY) {
        scrollOffset.y = -areaOvershoot.y * Math.min(Math.max(0, scrollY), 1);
    }

    private double getRelativeScrollX() {
        return areaOvershoot.x <= 0.0 ? 0 : -scrollOffset.x / areaOvershoot.x;
    }

    private double getRelativeScrollY() {
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
        return new UIScrollbarContainerWrapper(HORIZONTAL, container, this);
    }

    private UIContainer wrapVScroll(UIContainer container) {
        return new UIScrollbarContainerWrapper(VERTICAL, container, this);
    }

    public void setHideScrollbars(boolean hideScrollbars) {
        this.hideScrollbars = hideScrollbars;
    }

    private boolean hideScrollbar(int orientation) {
        if (!hideScrollbars || areaOvershoot == null) {
            return true;
        }
        return orientation == VERTICAL
                ? isVScroll() && areaOvershoot.y <= 0.0
                : isHScroll() && areaOvershoot.x <= 0.0;
    }

    private static class UIScrollbarContainerWrapper extends UIContainer {

        UIScrollBarContainer scrollBarContainer;

        UIScrollbarContainerWrapper(int orientation, UIContainer container, UIScrollArea scrollArea) {
            super(1 - orientation, 0);

            noBackground().noOutline();
            zeroMargin().zeroPadding().setMinimalSize();

            add(container);
            scrollBarContainer = new UIScrollBarContainer(scrollArea, orientation);
            add(scrollBarContainer);
        }
    }

    private static class UIScrollBarContainer extends UIDragContainer<UIScrollBar> {

        UIScrollArea scrollArea;

        UIScrollBarContainer(UIScrollArea scrollArea, int orientation) {
            super(new UIScrollBar(scrollArea, orientation));

            this.orientation = orientation;
            this.scrollArea = scrollArea;

            draggable.setScrollBarContainer(this);

            withOutline();
            withBackground();
            zeroMargin();

            if (orientation == VERTICAL) {
                setVFillSize();
            } else {
                setHFillSize();
            }
        }

        @Override
        public boolean isVisible() {
            return !scrollArea.hideScrollbar(orientation);
        }

        @Override
        public void expandAsNeccessary(SVector remainingSize) {
            super.expandAsNeccessary(remainingSize);

            draggable.expandAsNeccessary();
        }
    }

    private static class UIScrollBar extends UIElement implements Draggable {

        private int orientation;

        private UIScrollArea scrollArea;
        private UIScrollBarContainer scrollBarContainer;

        UIScrollBar(UIScrollArea scrollArea, int orientation) {
            this.scrollArea = scrollArea;
            this.orientation = orientation;

            UIStyle style = new UIStyle(
                    () -> mouseAbove || scrollBarContainer.isDragging()
                            ? panel.getOutlineNormalColor()
                            : panel.getOutlineHighlightColor(),
                    () -> null,
                    () -> 0.0);
            setStyle(style);
        }

        @Override
        public void setMinSize() {
            double min = 2 * panel.getMargin();
            size.set(min, min);
        }

        public void expandAsNeccessary() {
            if (orientation == VERTICAL) {
                size.y = scrollArea.getHeightFraction() * parent.size.y;
            } else {
                size.x = scrollArea.getWidthFraction() * parent.size.x;
            }
        }

        public void setScrollBarContainer(UIScrollBarContainer scrollBarContainer) {
            this.scrollBarContainer = scrollBarContainer;
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