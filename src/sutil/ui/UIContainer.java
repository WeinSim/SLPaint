package sutil.ui;

import java.util.ArrayList;

import sutil.math.SVector;

public class UIContainer extends UIElement {

    public static final int LEFT = 0, TOP = 0, CENTER = 1, RIGHT = 2, BOTTOM = 2;
    public static final int VERTICAL = 0, HORIZONTAL = 1;

    /**
     * Every {@code UIContainer} has one of four size types:
     * <ul>
     * <li>{@code MINIMAL}: The size of the container is the smallest size that fits
     * all of its children, the padding between the children and the margin around
     * the border.</li>
     * <li>{@code FIXED}: A fixed size is specified, which the container will always
     * have.</li>
     * <li>{@code FILL}: The container tries to fill up all available space in the
     * direction perpendicular to its parent's orientation (i.e. if it is
     * contained in a vertically aligned container, it expands horizontally but not
     * vertically).</li>
     * <li>{@code MAXIMAL}: The container tries to fill up all available space in
     * both directions.</li>
     * </ul>
     * 
     * <h3>Size type priorities</h3>
     * If a {@code MAXIMAL} container is placed inside of a {@code MINIMAL}
     * container, the {@code MINIMAL} container will effectively act as a
     * {@code MAXIMAL} container, i.e. it will also try to use all of the
     * available space of its parent. This chain continues upwards until a
     * {@code FIXED} or {@code FILL} container is reached.
     */
    protected enum SizeType {
        MINIMAL,
        FIXED,
        FILL,
        MAXIMAL;
    }

    protected int orientation;
    protected int alignment;

    protected SizeType sizeType;

    /**
     * Indicates if this container or one of its children is {@code MAXIMAL}.
     */
    private boolean effectivelyMaximal;

    protected double marginScale = 1;
    protected double paddingScale = 1;

    private ArrayList<UIElement> children;
    private ArrayList<UIElement> visibleChildren;

    protected boolean addSeparators = false;
    protected boolean addInitialSeparator = false;

    public UIContainer(int orientation, int alignment) {
        if (orientation < 0 || orientation >= 2 || alignment < 0 || alignment >= 3) {
            throw new IllegalArgumentException(String.format(
                    "Invalid orientation or alignment. orientation = %d, alignment = %d",
                    orientation, alignment));
        }

        this.orientation = orientation;
        this.alignment = alignment;

        children = new ArrayList<>();
        visibleChildren = new ArrayList<>();
        sizeType = SizeType.MINIMAL;

        outlineNormal = true;
        // backgroundNormal = true;
    }

    @Override
    public void setPanel(UIPanel panel) {
        super.setPanel(panel);
        for (UIElement child : children) {
            child.setPanel(panel);
        }
    }

    /**
     * Adds {@code child} as a non-floating child element.
     * 
     * @param child The child {@code UIElement} to add to this {@code UIContainer}.
     */
    public void add(UIElement child) {
        if (addSeparators) {
            if (addInitialSeparator || children.size() > 0) {
                UISeparator separator = new UISeparator();
                separator.setVisibilitySupplier(() -> child.isVisible());
                addActual(separator);
            }
        }

        addActual(child);
    }

    private void addActual(UIElement child) {
        if (children.contains(child)) {
            return;
        }

        children.add(child);
        child.setPanel(panel);
        child.parent = this;

        if (child.isVisible()) {
            visibleChildren.add(child);
        }
    }

    public void clearChildren() {
        children.clear();
        visibleChildren.clear();
    }

    @Override
    public void update(SVector mouse) {
        super.update(mouse);

        SVector relativeMouse = mouse == null ? null : new SVector(mouse).sub(position);
        for (UIElement child : getChildren()) {
            child.update(relativeMouse);
        }
    }

    @Override
    public void mousePressed(SVector mouse) {
        super.mousePressed(mouse);

        SVector relativeMouse = new SVector(mouse).sub(position);
        for (UIElement child : getChildren()) {
            child.mousePressed(relativeMouse);
        }
    }

    @Override
    public void mouseWheel(SVector scroll, SVector mousePos) {
        super.mouseWheel(scroll, mousePos);

        SVector relativeMouse = new SVector(mousePos).sub(position);
        for (UIElement child : getChildren()) {
            child.mouseWheel(scroll, relativeMouse);
        }
    }

    @Override
    public void keyPressed(char key) {
        super.keyPressed(key);

        for (UIElement child : getChildren()) {
            child.keyPressed(key);
        }
    }

    public void determineChildVisibility() {
        visibleChildren.clear();
        for (UIElement child : children) {
            if (child.isVisible()) {
                visibleChildren.add(child);

                if (child instanceof UIContainer container) {
                    container.determineChildVisibility();
                }
            }
        }
    }

    public void updateSizeReferences() {
        effectivelyMaximal = false;
        if (sizeType == SizeType.MAXIMAL) {
            UIContainer current = this;
            while (current.sizeType != SizeType.FIXED && current.sizeType != SizeType.FILL) {
                current.effectivelyMaximal = true;
                current = current.parent;
            }
        }
        for (UIElement child : getChildren()) {
            if (child instanceof UIContainer container) {
                container.updateSizeReferences();
            }
        }
    }

    @Override
    public void setMinSize() {
        for (UIElement child : getChildren()) {
            child.setMinSize();
        }

        if (sizeType != SizeType.FIXED) {
            double margin = getMargin();
            double padding = getPadding();
            SVector boundingBox = getChildrenBoundingBox(margin, padding, true);
            size.set(boundingBox);
        }
    }

    public void expandAsNeccessary(SVector remainingSize) {
        boolean expandX = false, expandY = false;
        if (effectivelyMaximal) {
            expandX = true;
            expandY = true;
        } else if (sizeType == SizeType.FILL) {
            switch (parent.orientation) {
                case VERTICAL -> expandX = true;
                case HORIZONTAL -> expandY = true;
            }
        }
        if (expandX) {
            size.x = remainingSize.x;
        }
        if (expandY) {
            size.y = remainingSize.y;
        }

        remainingSize = getRemainingSize();
        for (UIElement child : children) {
            if (child instanceof UIContainer container) {
                container.expandAsNeccessary(remainingSize);
            }
        }
    }

    protected SVector getRemainingSize() {
        double margin = getMargin(),
                padding = getPadding();
        SVector boundingBox = getChildrenBoundingBox(margin, padding, false);
        switch (orientation) {
            case VERTICAL -> boundingBox.x = 2 * margin;
            case HORIZONTAL -> boundingBox.y = 2 * margin;
        }
        return new SVector(size).sub(boundingBox);
    }

    public void positionChildren() {
        double margin = getMargin();
        double padding = getPadding();

        double runningTotal = margin;
        for (UIElement child : getChildren()) {
            SVector childPos = child.getPosition();
            SVector childSize = child.getSize();

            childPos.x = orientation == VERTICAL
                    ? margin + (size.x - 2 * margin - childSize.x) * alignment / 2.0
                    : runningTotal;
            childPos.y = orientation == VERTICAL
                    ? runningTotal
                    : margin + (size.y - 2 * margin - childSize.y) * alignment / 2.0;

            runningTotal += orientation == VERTICAL ? childSize.y : childSize.x;
            runningTotal += padding;

            if (child instanceof UIContainer container) {
                container.positionChildren();
            }
        }
    }

    protected SVector getChildrenBoundingBox(double margin, double padding, boolean includeMaxChildren) {
        double sum = 0;
        double max = 0;
        for (UIElement child : getChildren()) {
            if (!includeMaxChildren && child instanceof UIContainer container && container.effectivelyMaximal) {
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
        }
        sum += 2 * margin + Math.max(0, (getChildren().size() - 1)) * padding;
        max += 2 * margin;

        return switch (orientation) {
            case VERTICAL -> new SVector(max, sum);
            case HORIZONTAL -> new SVector(sum, max);
            default -> null;
        };
    }

    /**
     * 
     * @return the space around the outside.
     */
    public double getMargin() {
        return panel.getMargin() * marginScale;
    }

    /**
     * 
     * @return the space between the children.
     */
    public double getPadding() {
        return panel.getPadding() * paddingScale;
    }

    public void setMinimalSize() {
        sizeType = SizeType.MINIMAL;
    }

    public void setFixedSize(SVector size) {
        this.size.set(size);
        sizeType = SizeType.FIXED;
    }

    public void setFillSize() {
        sizeType = SizeType.FILL;
    }

    public void setMaximalSize() {
        sizeType = SizeType.MAXIMAL;
    }

    public int getOrientation() {
        return orientation;
    }

    public int getAlignment() {
        return alignment;
    }

    public ArrayList<UIElement> getChildren() {
        return visibleChildren;
    }

    public UIContainer setMarginScale(double marginScale) {
        this.marginScale = marginScale;
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
        setMarginScale(0);
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
        if (alignment < 0 || alignment >= 3) {
            throw new IllegalArgumentException(String.format("Invalid alignment (%d)", alignment));
        }
        this.alignment = alignment;

        return this;
    }

    public UIContainer withSeparators() {
        return withSeparators(true, false);
    }

    public UIContainer withSeparators(boolean initialSeparator) {
        return withSeparators(true, initialSeparator);
    }

    public UIContainer withSeparators(boolean addSeparators, boolean addInitialSeparator) {
        this.addSeparators = addSeparators;
        this.addInitialSeparator = addInitialSeparator;

        marginScale = 2.0;

        return this;
    }
}