package sutil.ui;

public class UISeparator extends UIContainer {

    private boolean forceZeroMargin;

    public UISeparator() {
        super(VERTICAL, LEFT);

        noOutline();
        forceZeroMargin = false;

        add(new UISeparatorInside());
    }

    @Override
    public void update() {
        super.update();

        if (parent.getOrientation() == VERTICAL) {
            setHFillSize();
            setVMinimalSize();
        } else {
            setHMinimalSize();
            setVFillSize();
        }

        if (!forceZeroMargin) {
            hMarginScale = parent.orientation == VERTICAL ? 0 : 1;
            vMarginScale = 1 - hMarginScale;
        }
    }

    @Override
    public UIContainer zeroMargin() {
        forceZeroMargin = true;

        return super.zeroMargin();
    }

    private class UISeparatorInside extends UIContainer {

        public UISeparatorInside() {
            super(VERTICAL, LEFT);
            setHFillSize();
            setVFillSize();
            zeroMargin();

            style.setStrokeColor(() -> panel.getSeparatorColor());
        }
    }
}