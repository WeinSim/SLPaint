package sutil.ui;

import java.util.ArrayList;

import sutil.math.SVector;

public class UIContainer extends UIElement {

    public static final int LEFT = 0, TOP = 0, CENTER = 1, RIGHT = 2, BOTTOM = 2;
    public static final int VERTICAL = 0, HORIZONTAL = 1;

    /**
     * Every {@code UIContainer} has one of four size types in both directions
     * (vertical and horizontal):
     * <ul>
     * <li>{@code MINIMAL}: The width / height of the container is the smallest
     * width / height that fits all of its children, the padding between the
     * children and the margin around the border.</li>
     * <li>{@code FIXED}: A fixed width / height is specified, which the container
     * will always have.</li>
     * <li>{@code FILL}: The container expands horizontally / vertically as much as
     * possible.</li>
     * <li>{@code MAXIMAL}: Like {@code FILL}, except it also turns any
     * {@code MIMINAL} ascestors effectively maximal.</li>
     * </ul>
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

    protected SVector minSize;

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

        minSize = new SVector();

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

    public void add(UIElement child) {
        if (addSeparators && !(child instanceof UIFloatContainer)) {
            if (addInitialSeparator || children.size() > 0) {
                UISeparator separator = new UISeparator();
                separator.setVisibilitySupplier(() -> child.isVisible());
                addActual(separator);
            }
        }

        addActual(child);
    }

    protected final void addActual(UIElement child) {
        if (children.contains(child)) {
            return;
        }

        if (child instanceof UIFloatContainer) {
            children.addFirst(child);
        } else {
            children.add(child);
        }
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
    public void update() {
        for (UIElement child : getChildren()) {
            child.update();
        }
    }

    @Override
    public void updateMousePosition(SVector mouse, boolean valid) {
        super.updateMousePosition(mouse, valid);

        boolean childMouseAbove = false;
        SVector relativeMouse = new SVector(mouse).sub(position);
        for (UIElement child : getChildren()) {
            if (!(child instanceof UIFloatContainer)) {
                child.updateMousePosition(relativeMouse, valid && !childMouseAbove);
                childMouseAbove |= child.mouseAbove();
            }
        }
    }

    @Override
    public void mousePressed(int mouseButton) {
        for (UIElement child : getChildren()) {
            child.mousePressed(mouseButton);
        }

        super.mousePressed(mouseButton);
    }

    @Override
    public boolean mouseWheel(SVector scroll, SVector mousePos) {
        SVector relativeMouse = new SVector(mousePos).sub(position);
        for (UIElement child : getChildren()) {
            if (child.mouseWheel(scroll, relativeMouse)) {
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

    public final void setMinSize() {
        for (UIElement child : getChildren()) {
            if (child instanceof UIContainer container) {
                container.setMinSize();
            } else {
                child.setPreferredSize();
            }
        }

        setSizeAccordingToBoundingBox();

        SVector override = overrideMinSize();
        if (override != null) {
            if (override.x > 0 && hSizeType != SizeType.FIXED) {
                size.x = override.x;
            }
            if (override.y > 0 && vSizeType != SizeType.FIXED) {
                size.y = override.y;
            }
        }

        minSize.set(size);
    }

    protected SVector overrideMinSize() {
        return null;
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
        adjustAcrossAxis();

        adjustAlongAxis();

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

        boolean expand = remainingSize > 0;

        ArrayList<UIContainer> hvChildren = new ArrayList<>();
        for (UIElement child : getChildren()) {
            if (child instanceof UIContainer container) {
                boolean condition = expand
                        ? (orientation == VERTICAL ? container.vEffectivelyMaximal : container.hEffectivelyMaximal)
                        : container.getSizeAlongAxis() > container.getMinSizeAlongAxis();
                if (condition) {
                    hvChildren.add(container);
                }
            }
        }

        if (expand) {
            expandChildren(hvChildren, remainingSize);
        } else {
            shrinkChildren(hvChildren, remainingSize);
        }
    }

    /**
     * 
     * @see https://github.com/nicbarker/clay/blob/main/clay.h#L2190
     */
    private void expandChildren(ArrayList<UIContainer> expandChildren, double remainingSize) {
        final double epsilon = 1e-6;
        if (expandChildren.isEmpty()) {
            return;
        }
        while (remainingSize > epsilon) {
            double smallest = expandChildren.get(0).getSizeAlongAxis();
            double secondSmallest = Double.POSITIVE_INFINITY;
            double sizeToAdd = remainingSize;

            for (UIContainer child : expandChildren) {
                double component = child.getSizeAlongAxis();
                if (lessThan(component, smallest, epsilon)) {
                    secondSmallest = smallest;
                    smallest = component;
                }
                if (lessThan(smallest, component, epsilon)) {
                    secondSmallest = Math.min(secondSmallest, component);
                    sizeToAdd = secondSmallest - smallest;
                }
            }

            sizeToAdd = Math.min(sizeToAdd, remainingSize / expandChildren.size());

            for (UIContainer child : expandChildren) {
                double component = child.getSizeAlongAxis();
                if (equalTo(component, smallest, epsilon)) {
                    child.setSizeAlongAxis(component + sizeToAdd);
                    remainingSize -= sizeToAdd;
                }
            }
        }
    }

    private void shrinkChildren(ArrayList<UIContainer> shrinkChildren, double remainingSize) {
        final double epsilon = 1e-6;
        if (shrinkChildren.isEmpty()) {
            return;
        }
        remainingSize *= -1;
        while (remainingSize > epsilon) {
            for (int i = shrinkChildren.size() - 1; i >= 0; i--) {
                UIContainer child = shrinkChildren.get(i);
                if (child.getSizeAlongAxis() - child.getMinSizeAlongAxis() < epsilon) {
                    shrinkChildren.remove(i);
                }
            }
            if (shrinkChildren.isEmpty()) {
                return;
            }

            double biggest = shrinkChildren.get(0).getSizeAlongAxis();
            double secondBiggest = Double.NEGATIVE_INFINITY;
            double sizeToRemove = remainingSize;

            for (UIContainer child : shrinkChildren) {
                double component = child.getSizeAlongAxis();
                if (lessThan(biggest, component, epsilon)) {
                    secondBiggest = biggest;
                    biggest = component;
                }
                if (lessThan(component, biggest, epsilon)) {
                    secondBiggest = Math.max(secondBiggest, component);
                    sizeToRemove = biggest - secondBiggest;
                }
            }

            sizeToRemove = Math.min(sizeToRemove, remainingSize / shrinkChildren.size());

            for (UIContainer child : shrinkChildren) {
                double component = child.getSizeAlongAxis();
                if (equalTo(component, biggest, epsilon)) {
                    double maxRemoveAmount = child.getSizeAlongAxis() - child.getMinSizeAlongAxis();
                    double removing = Math.min(maxRemoveAmount, sizeToRemove);
                    child.setSizeAlongAxis(component - removing);
                    remainingSize -= removing;
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
                    boolean effectivelyMaximal = orientation == VERTICAL
                            ? container.hEffectivelyMaximal
                            : container.vEffectivelyMaximal;
                    if (effectivelyMaximal) {
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
        return orientation == VERTICAL
                ? size.x - 2 * getHMargin()
                : size.y - 2 * getVMargin();
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