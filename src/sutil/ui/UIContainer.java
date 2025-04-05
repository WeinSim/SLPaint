package sutil.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

import sutil.math.SVector;

public class UIContainer extends UIElement {

    public static final int LEFT = 0, TOP = 0, CENTER = 1, RIGHT = 2, BOTTOM = 2;
    public static final int VERTICAL = 0, HORIZONTAL = 1, NONE = 2, BOTH = 3;

    /**
     * Every {@code UIContainer} has one of three size types in both directions
     * (vertical and horizontal):
     * <ul>
     * <li>{@code MINIMAL}: The width / height of the container is the smallest
     * width / height that fits all of its children, the padding between the
     * children and the margin around the border.</li>
     * <li>{@code FIXED}: A fixed width / height is specified, which the container
     * will always have.</li>
     * <li>{@code FILL}: The container expands horizontally / vertically as much as
     * possible, withouth affecting its parent container.</li>
     * </ul>
     */
    protected enum SizeType {
        MINIMAL,
        FIXED,
        FILL;
    }

    private ChildList children;

    protected int orientation;
    protected int hAlignment, vAlignment;

    private final int scrollMode;
    private SVector scrollOffset;
    private SVector areaOvershoot;
    /**
     * Indicates wether a scroll bar should be hidden if there is not enough content
     * to allow scrolling.
     */
    protected boolean hideScrollbars = true;

    protected SizeType hSizeType, vSizeType;

    protected SVector minSize;

    protected double hMarginScale = 1, vMarginScale = 1;
    protected double paddingScale = 1;

    protected boolean addSeparators = false;

    public UIContainer(int orientation, int alignment) {
        this(orientation, alignment, alignment, NONE);
    }

    public UIContainer(int orientation, int hAlignment, int vAlignment) {
        this(orientation, hAlignment, vAlignment, NONE);
    }

    public UIContainer(int orientation, int hAlignment, int vAlignment, int scrollMode) {
        super();

        this.scrollMode = scrollMode;

        outlineNormal = true;

        setOrientation(orientation);
        setHAlignment(hAlignment);
        setVAlignment(vAlignment);

        hSizeType = SizeType.MINIMAL;
        vSizeType = SizeType.MINIMAL;
        if (isHScroll()) {
            setHFillSize();
        }
        if (isVScroll()) {
            setVFillSize();
        }

        minSize = new SVector();

        children = new ChildList();

        scrollOffset = new SVector();
    }

    @Override
    public void setPanel(UIPanel panel) {
        super.setPanel(panel);

        for (UIElement child : children) {
            child.setPanel(panel);
        }
    }

    public void add(UIElement child) {
        if (addSeparators && !(child instanceof UIFloatContainer)) {
            // TODO: this doesn't produce the exepcted behavior if at this point, this
            // container already contains one or more float children
            if (children.size() > 0) {
                UISeparator separator = new UISeparator();
                separator.setVisibilitySupplier(child::isVisible);
                addActual(separator);
            }
        }

        addActual(child);
    }

    protected final void addActual(UIElement child) {
        if (child instanceof UIFloatContainer) {
            children.addFirst(child);
        } else {
            children.add(child);
        }
        child.parent = this;

        child.setPanel(panel);
    }

    public void lock() {
        children.lock();

        for (UIElement child : children.children) {
            if (child instanceof UIContainer container) {
                container.lock();
            }
        }
    }

    @Override
    public void updateVisibility() {
        super.updateVisibility();
        if (!isVisible()) {
            return;
        }

        for (UIElement child : children.children) {
            child.updateVisibility();
        }
    }

    @Override
    public void updateMousePosition(SVector mouse, boolean valid) {
        super.updateMousePosition(mouse, valid);

        boolean childMouseAbove = false;
        SVector relativeMouse = new SVector(mouse).sub(position);
        for (UIElement child : getChildren()) {
            if (!(child instanceof UIFloatContainer)) {
                child.updateMousePosition(relativeMouse, valid && mouseAbove() && !childMouseAbove);
                childMouseAbove |= child.mouseAbove();
            }
        }
    }

    @Override
    public void update() {
        super.update();

        for (UIElement child : getChildren()) {
            child.update();
        }
    }

    @Override
    public void mousePressed(int mouseButton) {
        super.mousePressed(mouseButton);

        for (UIElement child : getChildren()) {
            child.mousePressed(mouseButton);
        }
    }

    @Override
    public boolean mouseWheel(SVector scroll, SVector mousePos) {
        SVector relativeMouse = new SVector(mousePos).sub(position);
        for (UIElement child : getChildren()) {
            if (child.mouseWheel(scroll, relativeMouse)) {
                return true;
            }
        }

        if (mouseAbove()) {
            boolean doScroll = false;
            if (isHScroll()) {
                scrollOffset.x += scroll.x;
                doScroll = true;
            }
            if (isVScroll()) {
                scrollOffset.y += scroll.y;
                doScroll = true;
            }
            if (doScroll) {
                return true;
            }
        }

        return super.mouseWheel(scroll, mousePos);
    }

    @Override
    public void keyPressed(char key) {
        super.keyPressed(key);

        for (UIElement child : getChildren()) {
            child.keyPressed(key);
        }
    }

    public final void setMinSize() {
        for (UIElement child : getChildren()) {
            if (child instanceof UIContainer container) {
                container.setMinSize();
            } else {
                child.setPreferredSize();
            }
        }

        setSizeAccordingToBoundingBox();

        if (isHScroll() && hSizeType != SizeType.FIXED) {
            size.x = 4 * panel.getMargin();
        }
        if (isVScroll() && vSizeType != SizeType.FIXED) {
            size.y = 4 * panel.getMargin();
        }

        minSize.set(size);
    }

    @Override
    public final void setPreferredSize() {
        for (UIElement child : getChildren()) {
            child.setPreferredSize();
        }

        setSizeAccordingToBoundingBox();
    }

    private void setSizeAccordingToBoundingBox() {
        if (hSizeType == SizeType.FIXED && vSizeType == SizeType.FIXED) {
            return;
        }

        double hMargin = getHMargin(), vMargin = getVMargin();
        double padding = getPadding();
        SVector boundingBox = getChildrenBoundingBox(hMargin, vMargin, padding);

        if (hSizeType != SizeType.FIXED) {
            size.x = boundingBox.x;
        }
        if (vSizeType != SizeType.FIXED) {
            size.y = boundingBox.y;
        }
    }

    public void expandAsNeccessary() {
        // the order of these two doesn't matter
        adjustAlongAxis();
        adjustAcrossAxis();

        for (UIElement child : getChildren()) {
            if (child instanceof UIContainer container) {
                container.expandAsNeccessary();
            }
        }
    }

    private void adjustAlongAxis() {
        double hMargin = getHMargin(),
                vMargin = getVMargin();
        double padding = getPadding();
        SVector boundingBox = getChildrenBoundingBox(hMargin, vMargin, padding);
        double remainingSize = orientation == VERTICAL ? size.y - boundingBox.y : size.x - boundingBox.x;

        ArrayList<UIContainer> hvChildren = new ArrayList<>();
        if (remainingSize > 0) {
            // determine all children that can expand
            for (UIElement child : getChildren()) {
                if (child instanceof UIContainer container) {
                    if ((orientation == VERTICAL ? container.vSizeType : container.hSizeType) == SizeType.FILL) {
                        hvChildren.add(container);
                    }
                }
            }
        } else {
            // all children can shrink, but only up to minimumSize
            for (UIElement child : getChildren()) {
                if (child instanceof UIContainer container) {
                    hvChildren.add(container);
                }
            }
        }

        if (!hvChildren.isEmpty()) {
            expandOrShrinkChildren(hvChildren, remainingSize);
        }
    }

    /**
     * 
     * @see https://github.com/nicbarker/clay/blob/main/clay.h#L2190
     */
    private void expandOrShrinkChildren(ArrayList<UIContainer> containers, double remainingSize) {
        final double epsilon = 1e-6;

        int sign = remainingSize < 0 ? -1 : 1;
        remainingSize *= sign; // remaining size will now always be positive.

        while (remainingSize > epsilon) {
            if (sign == -1) {
                // containers cannot shrink below their minimum size
                for (int i = containers.size() - 1; i >= 0; i--) {
                    UIContainer child = containers.get(i);
                    if (child.getSizeAlongAxis() - child.getMinSizeAlongAxis() < epsilon) {
                        containers.remove(i);
                    }
                }
            }
            if (containers.isEmpty()) {
                return;
            }

            double smallest = containers.get(0).getSizeAlongAxis() * sign;
            double secondSmallest = Double.POSITIVE_INFINITY;
            double sizeToAdd = remainingSize; // or size to remove. always positive.

            for (UIContainer child : containers) {
                double component = child.getSizeAlongAxis() * sign;
                if (lessThan(component, smallest, epsilon)) {
                    secondSmallest = smallest;
                    smallest = component;
                }
                if (lessThan(smallest, component, epsilon)) {
                    secondSmallest = Math.min(secondSmallest, component);
                    sizeToAdd = secondSmallest - smallest;
                }
            }

            sizeToAdd = Math.min(sizeToAdd, remainingSize / containers.size());

            for (UIContainer child : containers) {
                double component = child.getSizeAlongAxis() * sign;
                if (equalTo(component, smallest, epsilon)) {
                    double adding; // or removing. always positive.
                    if (sign == 1) {
                        adding = sizeToAdd;
                    } else {
                        double maxRemoveAmount = child.getSizeAlongAxis() - child.getMinSizeAlongAxis();
                        adding = Math.min(maxRemoveAmount, sizeToAdd);
                    }

                    child.setSizeAlongAxis((component + adding) * sign);
                    remainingSize -= adding;
                }
            }
        }
    }

    private void adjustAcrossAxis() {
        double availableSpace = getAvailableSpaceAcrossAxis();
        for (UIElement child : getChildren()) {
            if (child instanceof UIContainer container) {
                if (container.getSizeAcrossAxis() < availableSpace) {
                    // expand
                    SizeType sizeTypeAcrossAxis = orientation == VERTICAL
                            ? container.hSizeType
                            : container.vSizeType;
                    if (sizeTypeAcrossAxis == SizeType.FILL) {
                        container.setSizeAcrossAxis(availableSpace);
                    }
                } else {
                    // shrink
                    double newWH = Math.max(availableSpace, container.getMinSizeAcrossAxis());
                    container.setSizeAcrossAxis(newWH);
                }
            }
        }
    }

    protected double getAvailableSpaceAcrossAxis() {
        boolean isScroll = orientation == VERTICAL ? isVScroll() : isHScroll();
        if (isScroll) {
            SVector boundingBox = getChildrenBoundingBox(getHMargin(), getVMargin(), getPadding());
            return orientation == VERTICAL
                    ? Math.max(size.x, boundingBox.x) - 2 * getHMargin()
                    : Math.max(size.y, boundingBox.y) - 2 * getVMargin();
        } else {
            return orientation == VERTICAL
                    ? size.x - 2 * getHMargin()
                    : size.y - 2 * getVMargin();
        }
    }

    private double getSizeAlongAxis() {
        return parent.orientation == VERTICAL ? size.y : size.x;
    }

    private double getSizeAcrossAxis() {
        return parent.orientation == VERTICAL ? size.x : size.y;
    }

    private double getMinSizeAlongAxis() {
        return parent.orientation == VERTICAL ? minSize.y : minSize.x;
    }

    private double getMinSizeAcrossAxis() {
        return parent.orientation == VERTICAL ? minSize.x : minSize.y;
    }

    private void setSizeAlongAxis(double wh) {
        if (parent.orientation == VERTICAL) {
            size.y = wh;
        } else {
            size.x = wh;
        }
    }

    private void setSizeAcrossAxis(double wh) {
        if (parent.orientation == VERTICAL) {
            size.x = wh;
        } else {
            size.y = wh;
        }
    }

    private boolean lessThan(double a, double b, double epsilon) {
        return a + epsilon < b;
    }

    private boolean equalTo(double a, double b, double epsilon) {
        double difference = a - b;
        return difference > -epsilon && difference < epsilon;
    }

    public void positionChildren() {
        double hMargin = getHMargin(), vMargin = getVMargin();
        double padding = getPadding();
        SVector boundingBox = getChildrenBoundingBox(hMargin, vMargin, padding);

        areaOvershoot = new SVector(
                Math.max(0, boundingBox.x - size.x),
                Math.max(0, boundingBox.y - size.y));

        scrollOffset.x = Math.min(0, Math.max(scrollOffset.x, -areaOvershoot.x));
        scrollOffset.y = Math.min(0, Math.max(scrollOffset.y, -areaOvershoot.y));

        double runningTotal = 0;
        for (UIElement child : getChildren()) {
            if (child instanceof UIFloatContainer floatContainer) {
                floatContainer.setPosition();
            } else {
                SVector childPos = child.getPosition();
                SVector childSize = child.getSize();

                childPos.x = orientation == VERTICAL
                        ? hMargin + (size.x - 2 * hMargin - childSize.x) * hAlignment / 2.0
                        : hMargin + runningTotal + (size.x - boundingBox.x) * hAlignment / 2.0;
                childPos.y = orientation == VERTICAL
                        ? vMargin + runningTotal + (size.y - boundingBox.y) * vAlignment / 2.0
                        : vMargin + (size.y - 2 * vMargin - childSize.y) * vAlignment / 2.0;

                runningTotal += orientation == VERTICAL ? childSize.y : childSize.x;
                runningTotal += padding;
            }

            if (child instanceof UIContainer container) {
                if (!(child instanceof UIFloatContainer)) {
                    child.getPosition().add(scrollOffset);
                }

                container.positionChildren();
            }
        }
    }

    protected SVector getChildrenBoundingBox(double hMargin, double vMargin, double padding) {
        double sum = 0;
        double max = 0;
        int numNonFloatChildren = 0;
        for (UIElement child : getChildren()) {
            if (child instanceof UIFloatContainer) {
                continue;
            }
            SVector childSize = child.getSize();
            if (orientation == VERTICAL) {
                max = Math.max(max, childSize.x);
                sum += childSize.y;
            } else {
                max = Math.max(max, childSize.y);
                sum += childSize.x;
            }

            numNonFloatChildren++;
        }

        sum += Math.max(0, (numNonFloatChildren - 1)) * padding;
        SVector ret = switch (orientation) {
            case VERTICAL -> new SVector(max, sum);
            case HORIZONTAL -> new SVector(sum, max);
            default -> null;
        };
        ret.x += 2 * hMargin;
        ret.y += 2 * vMargin;

        return ret;
    }

    /**
     * 
     * @return the space around the outside (left and right).
     */
    public final double getHMargin() {
        return panel.getMargin() * hMarginScale;
    }

    /**
     * 
     * @return the space around the outside (top and bottom).
     */
    public final double getVMargin() {
        return panel.getMargin() * vMarginScale;
    }

    /**
     * 
     * @return the space between the children.
     */
    public final double getPadding() {
        return panel.getPadding() * paddingScale;
    }

    public UIContainer setMinimalSize() {
        hSizeType = SizeType.MINIMAL;
        vSizeType = SizeType.MINIMAL;
        return this;
    }

    public UIContainer setHMinimalSize() {
        hSizeType = SizeType.MINIMAL;
        return this;
    }

    public UIContainer setVMinimalSize() {
        vSizeType = SizeType.MINIMAL;
        return this;
    }

    public UIContainer setFixedSize(SVector size) {
        this.size.set(size);
        hSizeType = SizeType.FIXED;
        vSizeType = SizeType.FIXED;
        return this;
    }

    public UIContainer setHFixedSize(double width) {
        size.x = width;
        hSizeType = SizeType.FIXED;
        return this;
    }

    public UIContainer setVFixedSize(double height) {
        size.y = height;
        vSizeType = SizeType.FIXED;
        return this;
    }

    public UIContainer setFillSize() {
        hSizeType = SizeType.FILL;
        vSizeType = SizeType.FILL;
        return this;
    }

    public UIContainer setHFillSize() {
        hSizeType = SizeType.FILL;
        return this;
    }

    public UIContainer setVFillSize() {
        vSizeType = SizeType.FILL;
        return this;
    }

    public int getOrientation() {
        return orientation;
    }

    public Iterable<UIElement> getChildren() {
        return children;
    }

    public UIContainer setMarginScale(double marginScale) {
        hMarginScale = marginScale;
        vMarginScale = marginScale;
        return this;
    }

    public UIContainer setHMarginScale(double hMarginScale) {
        this.hMarginScale = hMarginScale;
        return this;
    }

    public UIContainer setVMarginScale(double vMarginScale) {
        this.vMarginScale = vMarginScale;
        return this;
    }

    public UIContainer setPaddingScale(double paddingScale) {
        this.paddingScale = paddingScale;
        return this;
    }

    /**
     * Removes space around the outside.
     * 
     * @param zeroMargin
     */
    public UIContainer zeroMargin() {
        setHMarginScale(0);
        setVMarginScale(0);
        return this;
    }

    /**
     * Removes space between children.
     * 
     * @return
     */
    public UIContainer zeroPadding() {
        setPaddingScale(0);
        return this;
    }

    public UIContainer setOrientation(int orientation) {
        if (orientation < 0 || orientation >= 2) {
            throw new IllegalArgumentException(String.format("Invalid orientation (%d)", orientation));
        }
        this.orientation = orientation;

        return this;
    }

    public UIContainer setAlignment(int alignment) {
        return setAlignment(alignment, alignment);
    }

    public UIContainer setAlignment(int hAlignment, int vAlignment) {
        setHAlignment(hAlignment);
        setVAlignment(vAlignment);
        return this;
    }

    public UIContainer setHAlignment(int hAlignment) {
        if (hAlignment < 0 || hAlignment >= 3) {
            throw new IllegalArgumentException(String.format("Invalid horizontal alignment: %d", hAlignment));
        }
        this.hAlignment = hAlignment;

        return this;
    }

    public UIContainer setVAlignment(int vAlignment) {
        if (vAlignment < 0 || vAlignment >= 3) {
            throw new IllegalArgumentException(String.format("Invalid vertical alignment: %d", vAlignment));
        }
        this.vAlignment = vAlignment;

        return this;
    }

    public UIContainer withSeparators() {
        addSeparators = true;

        setMarginScale(2.0);

        return this;
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

    public boolean isHScroll() {
        return switch (scrollMode) {
            case HORIZONTAL, BOTH -> true;
            default -> false;
        };
    }

    public boolean isVScroll() {
        return switch (scrollMode) {
            case VERTICAL, BOTH -> true;
            default -> false;
        };
    }

    public UIContainer addScrollbars() {
        UIContainer ret = this;
        if (isHScroll()) {
            ret = wrapHScroll(this);
        }
        if (isVScroll()) {
            ret = wrapVScroll(ret);
        }
        return ret;
    }

    private UIContainer wrapHScroll(UIContainer container) {
        return new UIScrollbarContainerWrapper(HORIZONTAL, container, this);
    }

    private UIContainer wrapVScroll(UIContainer container) {
        return new UIScrollbarContainerWrapper(VERTICAL, container, this);
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

        UIContainer scrollArea;
        UIScrollbarContainer scrollbarContainer;

        UIScrollbarContainerWrapper(int orientation, UIContainer container, UIContainer scrollArea) {
            super(1 - orientation, 0);

            this.scrollArea = scrollArea;

            noBackground().noOutline();
            zeroMargin().zeroPadding();

            add(container);
            scrollbarContainer = new UIScrollbarContainer(scrollArea, orientation);
            add(scrollbarContainer);
        }

        @Override
        public void update() {
            super.update();

            hSizeType = scrollArea.hSizeType;
            if (hSizeType == SizeType.FIXED) {
                hSizeType = SizeType.MINIMAL;
            }
            vSizeType = scrollArea.vSizeType;
            if (vSizeType == SizeType.FIXED) {
                vSizeType = SizeType.MINIMAL;
            }
        }
    }

    private static class UIScrollbarContainer extends UIDragContainer<UIScrollbar> {

        UIScrollbarContainer(UIContainer scrollArea, int orientation) {
            super(new UIScrollbar(scrollArea, orientation));

            this.orientation = orientation;

            draggable.setScrollbarContainer(this);

            withOutline();
            withBackground();
            zeroMargin();

            if (orientation == VERTICAL) {
                setVFillSize();
            } else {
                setHFillSize();
            }

            setVisibilitySupplier(() -> !scrollArea.hideScrollbar(orientation));
        }

        @Override
        public void expandAsNeccessary() {
            super.expandAsNeccessary();

            draggable.expandAsNeccessary();
        }
    }

    private static class UIScrollbar extends UIElement implements Draggable {

        private int orientation;

        private UIContainer scrollArea;
        private UIScrollbarContainer scrollbarContainer;

        UIScrollbar(UIContainer scrollArea, int orientation) {
            this.scrollArea = scrollArea;
            this.orientation = orientation;

            UIStyle style = new UIStyle(
                    () -> mouseAbove || scrollbarContainer.isDragging()
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

        public void setScrollbarContainer(UIScrollbarContainer scrollbarContainer) {
            this.scrollbarContainer = scrollbarContainer;
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

    private class ChildList implements Iterable<UIElement> {

        private ArrayList<UIElement> children;

        private boolean locked = false;

        ChildList() {
            children = new ArrayList<>();
        }

        void add(UIElement child) {
            checkNotLocked();
            children.add(child);
        }

        void addFirst(UIElement child) {
            checkNotLocked();
            children.addFirst(child);
        }

        int size() {
            return children.size();
        }

        void checkNotLocked() {
            if (locked) {
                throw new IllegalStateException("Cannot add children to UIContaienr after it has been locked.");
            }
        }

        void lock() {
            locked = true;
        }

        @Override
        public Iterator<UIElement> iterator() {
            return new Iterator<UIElement>() {

                private final Iterator<UIElement> inner = children.iterator();
                private UIElement nextVisible = null;

                @Override
                public boolean hasNext() {
                    while (inner.hasNext()) {
                        UIElement next = inner.next();
                        if (next.isVisible()) {
                            nextVisible = next;
                            return true;
                        }
                    }
                    return false;
                }

                @Override
                public UIElement next() {
                    if (nextVisible != null || hasNext()) {
                        UIElement result = nextVisible;
                        nextVisible = null;
                        return result;
                    }
                    throw new NoSuchElementException();
                }
            };
        }
    }
}