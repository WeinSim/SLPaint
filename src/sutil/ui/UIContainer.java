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
    protected int hAlignment, vAlignment;

    protected SizeType hSizeType, vSizeType;

    /**
     * Indicates wether this container or one of its children is {@code MAXIMAL} in
     * the corresponding direction.
     */
    private boolean hEffectivelyMaximal, vEffectivelyMaximal;

    protected double hMarginScale = 1, vMarginScale = 1;
    protected double paddingScale = 1;

    private ArrayList<UIElement> children;
    private ArrayList<UIElement> visibleChildren;

    protected boolean addSeparators = false;
    protected boolean addInitialSeparator = false;

    public UIContainer(int orientation, int alignment) {
        this(orientation, alignment, alignment);
    }

    public UIContainer(int orientation, int hAlignment, int vAlignment) {
        setOrientation(orientation);
        setHAlignment(hAlignment);
        setVAlignment(vAlignment);

        children = new ArrayList<>();
        visibleChildren = new ArrayList<>();

        hSizeType = SizeType.MINIMAL;
        vSizeType = SizeType.MINIMAL;

        outlineNormal = true;
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
        hEffectivelyMaximal = false;
        if (hSizeType == SizeType.MAXIMAL || hSizeType == SizeType.FILL) {
            UIContainer current = this;
            do {
                current.hEffectivelyMaximal = true;
                current = current.parent;
            } while (current != null
                    && hSizeType == SizeType.MAXIMAL
                    && current.hSizeType == SizeType.MINIMAL);
        }

        vEffectivelyMaximal = false;
        if (vSizeType == SizeType.MAXIMAL || vSizeType == SizeType.FILL) {
            UIContainer current = this;
            do {
                current.vEffectivelyMaximal = true;
                current = current.parent;
            } while (current != null
                    && vSizeType == SizeType.MAXIMAL
                    && current.vSizeType == SizeType.MINIMAL);
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

        if (hSizeType == SizeType.FIXED && vSizeType == SizeType.FIXED) {
            return;
        }

        double hMargin = getHMargin(), vMargin = getVMargin();
        double padding = getPadding();
        SVector boundingBox = getChildrenBoundingBox(hMargin, vMargin, padding, true);

        if (hSizeType != SizeType.FIXED) {
            size.x = boundingBox.x;
        }
        if (vSizeType != SizeType.FIXED) {
            size.y = boundingBox.y;
        }
    }

    public void expandAsNeccessary(SVector remainingSize) {
        if (hEffectivelyMaximal) {
            size.x = remainingSize.x;
            // if (parent.orientation == HORIZONTAL) {
            // remainingSize.x = 0;
            // }
        }
        if (vEffectivelyMaximal) {
            size.y = remainingSize.y;
            // if (parent.orientation == VERTICAL) {
            // remainingSize.y = 0;
            // }
        }

        remainingSize = getRemainingSize();
        for (UIElement child : children) {
            if (child instanceof UIContainer container) {
                container.expandAsNeccessary(remainingSize);
            }
        }
    }

    protected SVector getRemainingSize() {
        double hMargin = getHMargin(),
                vMargin = getVMargin();
        double padding = getPadding();
        SVector boundingBox = getChildrenBoundingBox(hMargin, vMargin, padding, false);
        switch (orientation) {
            case VERTICAL -> boundingBox.x = 2 * hMargin;
            case HORIZONTAL -> boundingBox.y = 2 * vMargin;
        }
        return new SVector(size).sub(boundingBox);
    }

    public void positionChildren() {
        double hMargin = getHMargin(), vMargin = getVMargin();
        double padding = getPadding();
        SVector boundingBox = getChildrenBoundingBox(hMargin, vMargin, padding, true);

        double runningTotal = 0;
        for (UIElement child : getChildren()) {
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

            if (child instanceof UIContainer container) {
                container.positionChildren();
            }
        }
    }

    protected SVector getChildrenBoundingBox(double hMargin, double vMargin, double padding,
            boolean includeMaxChildren) {

        double sum = 0;
        double max = 0;
        for (UIElement child : getChildren()) {
            if (!includeMaxChildren
                    && child instanceof UIContainer container
                    && (orientation == VERTICAL ? container.vEffectivelyMaximal : container.hEffectivelyMaximal)) {
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

        sum += Math.max(0, (getChildren().size() - 1)) * padding;
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
    public double getHMargin() {
        return panel.getMargin() * hMarginScale;
    }

    /**
     * 
     * @return the space around the outside (top and bottom).
     */
    public double getVMargin() {
        return panel.getMargin() * vMarginScale;
    }

    /**
     * 
     * @return the space between the children.
     */
    public double getPadding() {
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

    // public UIContainer setFillSize() {
    // hSizeType = SizeType.FILL;
    // vSizeType = SizeType.FILL;
    // return this;
    // }

    public UIContainer setHFillSize() {
        hSizeType = SizeType.FILL;
        return this;
    }

    public UIContainer setVFillSize() {
        vSizeType = SizeType.FILL;
        return this;
    }

    public UIContainer setMaximalSize() {
        hSizeType = SizeType.MAXIMAL;
        vSizeType = SizeType.MAXIMAL;
        return this;
    }

    public UIContainer setHMaximalSize() {
        hSizeType = SizeType.MAXIMAL;
        return this;
    }

    public UIContainer setVMaximalSize() {
        vSizeType = SizeType.MAXIMAL;
        return this;
    }

    public int getOrientation() {
        return orientation;
    }

    public ArrayList<UIElement> getChildren() {
        return visibleChildren;
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
        return withSeparators(true, false);
    }

    public UIContainer withSeparators(boolean initialSeparator) {
        return withSeparators(true, initialSeparator);
    }

    public UIContainer withSeparators(boolean addSeparators, boolean addInitialSeparator) {
        this.addSeparators = addSeparators;
        this.addInitialSeparator = addInitialSeparator;

        setMarginScale(2.0);

        return this;
    }
}