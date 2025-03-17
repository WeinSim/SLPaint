package sutil.ui;

import sutil.math.SVector;

public class UIScrollArea extends UIContainer {

    public static final int NONE = 2, BOTH = 3;

    private SVector scrollOffset;
    private SVector areaOvershoot;
    private final int scrollMode;

    /**
     * Indicates wether a scroll bar should be hidden if there is not enough content
     * to allow scrolling.
     */
    private boolean hideScrollbars;

    public UIScrollArea(int orientation, int alignment, int scrollMode) {
        super(orientation, alignment);
        this.scrollMode = scrollMode;

        if (isHScroll()) {
            setHMaximalSize();
        }
        if (isVScroll()) {
            setVMaximalSize();
        }

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
    protected SVector overrideMinSize() {
        SVector ret = new SVector(-1, -1);

        if (isHScroll()) {
            ret.x = 4 * panel.getMargin();
        }
        if (isVScroll()) {
            ret.y = 4 * panel.getMargin();
        }

        return ret;
    }

    @Override
    protected double getAvailableSpaceAcrossAxis() {
        SVector boundingBox = getChildrenBoundingBox(getHMargin(), getVMargin(), getPadding());
        return orientation == VERTICAL
                ? Math.max(size.x, boundingBox.x) - 2 * getHMargin()
                : Math.max(size.y, boundingBox.y) - 2 * getVMargin();
    }

    @Override
    public void positionChildren() {
        SVector boundingBox = getChildrenBoundingBox(getHMargin(), getVMargin(), getPadding());
        areaOvershoot = new SVector(
                Math.max(0, boundingBox.x - size.x),
                Math.max(0, boundingBox.y - size.y));

        scrollOffset.x = Math.min(0, Math.max(scrollOffset.x, -areaOvershoot.x));
        scrollOffset.y = Math.min(0, Math.max(scrollOffset.y, -areaOvershoot.y));

        super.positionChildren();

        for (UIElement child : getChildren()) {
            child.getPosition().add(scrollOffset);
        }
    }

    @Override
    public void mouseWheel(SVector scroll, SVector mousePos) {
        super.mouseWheel(scroll, mousePos);

        if (mouseAbove()) {
            scrollOffset.x += isHScroll() ? scroll.x : 0;
            scrollOffset.y += isVScroll() ? scroll.y : 0;
        }
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

    // @Override
    // public void setMinSize() {
    // for (UIElement child : getChildren()) {
    // child.setMinSize();
    // }

    // if (hSizeType == SizeType.FIXED && vSizeType == SizeType.FIXED) {
    // return;
    // }

    // double hMargin = getHMargin(), vMargin = getVMargin();
    // double padding = getPadding();
    // SVector boundingBox = getChildrenBoundingBox(hMargin, vMargin, padding,
    // true);

    // if (hSizeType != SizeType.FIXED) {
    // if (isHScroll()) {
    // size.x = 2 * hMargin;
    // } else {
    // size.x = boundingBox.x;
    // }
    // }
    // if (vSizeType != SizeType.FIXED) {
    // if (isVScroll()) {
    // size.y = 2 * vMargin;
    // } else {
    // size.y = boundingBox.y;
    // }
    // }
    // }

    @Override
    public UIContainer setHMinimalSize() {
        if (isHScroll()) {
            throw new IllegalArgumentException(
                    "A ScrollArea's SizeType cannot be MINIMAL in the direction of scrolling.");
        }
        return super.setHMinimalSize();
    }

    @Override
    public UIContainer setVMinimalSize() {
        if (isVScroll()) {
            throw new IllegalArgumentException(
                    "A ScrollArea's SizeType cannot be MINIMAL in the direction of scrolling.");
        }
        return super.setVMinimalSize();
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

        UIScrollArea scrollArea;
        UIScrollBarContainer scrollBarContainer;

        UIScrollbarContainerWrapper(int orientation, UIContainer container, UIScrollArea scrollArea) {
            super(1 - orientation, 0);

            this.scrollArea = scrollArea;

            noBackground().noOutline();
            zeroMargin().zeroPadding();

            add(container);
            scrollBarContainer = new UIScrollBarContainer(scrollArea, orientation);
            add(scrollBarContainer);
        }

        @Override
        public void update(SVector mouse) {
            super.update(mouse);

            if (orientation == VERTICAL) {
                hSizeType = scrollArea.hSizeType;
                if (hSizeType == SizeType.FIXED) {
                    hSizeType = SizeType.MINIMAL;
                }
            } else {
                vSizeType = scrollArea.vSizeType;
                if (vSizeType == SizeType.FIXED) {
                    vSizeType = SizeType.MINIMAL;
                }
            }
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
        public void expandAsNeccessary() {
            super.expandAsNeccessary();

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
        public void setPreferredSize() {
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