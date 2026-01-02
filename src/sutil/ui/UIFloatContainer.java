package sutil.ui;

import java.util.ArrayList;

import sutil.math.SVector;

public class UIFloatContainer extends UIContainer {

    public static final int TOP_LEFT = 0, TOP_RIGHT = 1, BOTTOM_LEFT = 2, BOTTOM_RIGHT = 3;

    private ArrayList<PositionSupplier> positionSuppliers;

    protected boolean clipToRoot;

    public UIFloatContainer(int orientation, int alignment) {
        super(orientation, alignment);

        withBackground();

        relativeLayer = 1;

        clipToRoot = true;

        positionSuppliers = new ArrayList<>();
    }

    @Override
    public boolean updateMouseAbove(boolean valid, boolean insideParent, int currentLayer, int targetLayer) {
        // A FloatContainer does not need to be inside its parent.
        return super.updateMouseAbove(valid, true, currentLayer, targetLayer);
    }

    public void clipToRoot(boolean clipToRoot) {
        this.clipToRoot = clipToRoot;
    }

    public UIFloatContainer clearAttachPoints() {
        positionSuppliers.clear();

        return this;
    }

    public UIFloatContainer addAttachPoint(int attachPoint, UIElement parent, int parentAttachPoint) {
        positionSuppliers.add(new PositionSupplier(attachPoint, parent, parentAttachPoint));

        return this;
    }

    public UIFloatContainer addAttachPoint(int attachPoint, SVector position) {
        positionSuppliers.add(new PositionSupplier(attachPoint, position));

        return this;
    }

    public void setPosition() {
        SVector minAbsolutePos = new SVector(0, 0);
        double minDistSq = Double.POSITIVE_INFINITY;

        UIRoot root = panel.getRoot();
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

        final int attachPoint;

        final SVector position;

        final UIElement parent;
        final int parentAttachPoint;

        PositionSupplier(int attachPoint, SVector position) {
            this.attachPoint = attachPoint;
            this.position = new SVector(position);

            parent = null;
            parentAttachPoint = 0;
        }

        PositionSupplier(int attachPoint, UIElement parent, int parentAttachPoint) {
            this.attachPoint = attachPoint;
            this.parent = parent;
            this.parentAttachPoint = parentAttachPoint;

            position = null;
        }

        SVector getAbsolutePosition() {
            SVector absolutePos;
            if (parent != null) {
                absolutePos = parent.getAbsolutePosition();
                if (parentAttachPoint == BOTTOM_LEFT || parentAttachPoint == BOTTOM_RIGHT) {
                    absolutePos.y += parent.getSize().y;
                }
                if (parentAttachPoint == TOP_RIGHT || parentAttachPoint == BOTTOM_RIGHT) {
                    absolutePos.x += parent.getSize().x;
                }
            } else {
                absolutePos = UIFloatContainer.this.parent.getAbsolutePosition().add(position);
            }

            if (attachPoint == BOTTOM_LEFT || attachPoint == BOTTOM_RIGHT) {
                absolutePos.y -= size.y;
            }
            if (attachPoint == TOP_RIGHT || attachPoint == BOTTOM_RIGHT) {
                absolutePos.x -= size.x;
            }

            return absolutePos;
        }
    }
}