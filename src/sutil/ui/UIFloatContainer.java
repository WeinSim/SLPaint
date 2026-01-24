package sutil.ui;

import java.util.ArrayList;
import java.util.function.Supplier;

import sutil.math.SVector;

public class UIFloatContainer extends UIContainer {

    public enum Anchor {

        TOP_LEFT(0, 0),
        TOP_CENTER(1, 0),
        TOP_RIGHT(2, 0),
        CENTER_LEFT(0, 1),
        CENTER_CENTER(1, 1),
        CENTER_RIGHT(2, 1),
        BOTTOM_LEFT(0, 2),
        BOTTOM_CENTER(1, 2),
        BOTTOM_RIGHT(2, 2);

        public final int dx, dy;

        private Anchor(int dx, int dy) {
            this.dx = dx;
            this.dy = dy;
        }

        public static Anchor fromOffsets(int dx, int dy) {
            for (Anchor anchor : values()) {
                if (anchor.dx == dx && anchor.dy == dy)
                    return anchor;
            }
            return null;
        }
    }

    private ArrayList<PositionSupplier> positionSuppliers;

    protected boolean clipToRoot;

    public UIFloatContainer(int orientation, int alignment) {
        super(orientation, alignment);

        withBackground();

        relativeLayer = 1;
        clipToRoot = false;

        positionSuppliers = new ArrayList<>();
    }

    public void clipToRoot(boolean clipToRoot) {
        this.clipToRoot = clipToRoot;
    }

    public UIFloatContainer clearAnchors() {
        positionSuppliers.clear();
        return this;
    }

    public UIFloatContainer addAnchor(Anchor anchor, UIElement parent, Anchor parentAnchor) {
        positionSuppliers.add(new PositionSupplier(anchor, parent, parentAnchor));
        return this;
    }

    public UIFloatContainer addAnchor(Anchor anchor, SVector position) {
        positionSuppliers.add(new PositionSupplier(anchor, position));
        return this;
    }

    public UIFloatContainer addAnchor(Anchor anchor, Supplier<SVector> positionSupplier) {
        positionSuppliers.add(new PositionSupplier(anchor, positionSupplier));
        return this;
    }

    public void setPosition() {
        SVector minAbsolutePos = new SVector(0, 0);
        double minDistSq = Double.POSITIVE_INFINITY;

        UIRoot root = UI.getRoot();
        for (PositionSupplier positionSupplier : positionSuppliers) {
            SVector absolutePos = positionSupplier.getAbsolutePosition();
            SVector originalAbsolutePos = new SVector(absolutePos);

            absolutePos.x = Math.max(0, Math.min(root.size.x - size.x, absolutePos.x));
            absolutePos.y = Math.max(0, Math.min(root.size.y - size.y, absolutePos.y));

            double distSq = absolutePos.distSq(originalAbsolutePos);
            if (distSq < minDistSq) {
                minDistSq = distSq;
                minAbsolutePos = clipToRoot ? absolutePos : originalAbsolutePos;
            }

            if (distSq == 0.0)
                break;
        }

        SVector parentAbsolutePos = parent.getAbsolutePosition();
        position.set(minAbsolutePos).sub(parentAbsolutePos);
    }

    @Override
    public UIContainer setFillSize() {
        throw new IllegalArgumentException("A UIFloatContainer's sizeType must be either MINIMAL or FIXED");
    }

    @Override
    public UIContainer setHFillSize() {
        throw new IllegalArgumentException("A UIFloatContainer's sizeType must be either MINIMAL or FIXED");
    }

    @Override
    public UIContainer setVFillSize() {
        throw new IllegalArgumentException("A UIFloatContainer's sizeType must be either MINIMAL or FIXED");
    }

    private class PositionSupplier {

        final Anchor anchor;

        final UIElement parent;
        final Anchor parentAnchor;

        final SVector position;
        final Supplier<SVector> positionSupplier;

        public PositionSupplier(Anchor anchor, UIElement parent, Anchor parentAnchor) {
            this.anchor = anchor;
            this.parent = parent;
            this.parentAnchor = parentAnchor;

            position = null;
            positionSupplier = null;
        }

        public PositionSupplier(Anchor anchor, SVector position) {
            this.anchor = anchor;
            this.position = new SVector(position);

            positionSupplier = null;
            parent = null;
            parentAnchor = null;
        }

        public PositionSupplier(Anchor anchor, Supplier<SVector> positionSupplier) {
            this.anchor = anchor;
            this.positionSupplier = positionSupplier;

            position = null;
            parent = null;
            parentAnchor = null;
        }

        SVector getAbsolutePosition() {
            SVector absolutePos;
            if (parent != null) {
                absolutePos = parent.getAbsolutePosition();
                SVector parentSize = parent.getSize();

                absolutePos.x += parentSize.x * parentAnchor.dx * 0.5;
                absolutePos.y += parentSize.y * parentAnchor.dy * 0.5;
            } else {
                absolutePos = UIFloatContainer.this.parent.getAbsolutePosition();
                absolutePos.add(position != null ? position : positionSupplier.get());
            }

            absolutePos.x -= size.x * anchor.dx * 0.5;
            absolutePos.y -= size.y * anchor.dy * 0.5;

            return absolutePos;
        }
    }
}