package sutil.ui;

import java.util.function.Supplier;

import sutil.math.SVector;

public class UIFloatContainer extends UIContainer {

    private Supplier<SVector> positionGetter;

    protected boolean clipToRoot;

    public UIFloatContainer(UIPanel panel, int orientation, int alignment, SVector position) {
        this(panel, orientation, alignment, (Supplier<SVector>) null);
        this.position.set(position);
    }

    public UIFloatContainer(UIPanel panel, int orientation, int alignment, Supplier<SVector> positionGetter) {
        super(orientation, alignment);
        this.positionGetter = positionGetter;
        this.panel = panel;

        withBackground();

        clipToRoot = true;
    }

    public void clipToRoot(boolean clipToRoot) {
        this.clipToRoot = clipToRoot;
    }

    public void setPositionGetter(Supplier<SVector> positionGetter) {
        this.positionGetter = positionGetter;
    }

    public void setPosition() {
        position.set(positionGetter.get());

        if (clipToRoot) {
            UIRoot root = panel.getRoot();
            SVector clipSize = root.size;
            SVector parentAbsolutePos = parent.getAbsolutePosition();

            position.x = Math.max(-parentAbsolutePos.x, Math.min(clipSize.x - parentAbsolutePos.x - size.x, position.x));
            position.y = Math.max(-parentAbsolutePos.y, Math.min(clipSize.y - parentAbsolutePos.y - size.y, position.y));
        }
    }

    @Override
    public UIContainer setHFillSize() {
        throw new IllegalArgumentException("A UIFloatContainer's sizeType must be either MINIMAL or FIXED");
    }

    @Override
    public UIContainer setVFillSize() {
        throw new IllegalArgumentException("A UIFloatContainer's sizeType must be either MINIMAL or FIXED");
    }

    @Override
    public UIContainer setHMaximalSize() {
        throw new IllegalArgumentException("A UIFloatContainer's sizeType must be either MINIMAL or FIXED");
    }

    @Override
    public UIContainer setVMaximalSize() {
        throw new IllegalArgumentException("A UIFloatContainer's sizeType must be either MINIMAL or FIXED");
    }
}