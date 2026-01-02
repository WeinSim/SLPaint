package sutil.ui;

public class UISeparator extends UIContainer {

    public UISeparator() {
        super(VERTICAL, LEFT);
        outlineNormal = false;

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

        hMarginScale = parent.orientation == VERTICAL ? 0 : 1;
        vMarginScale = 1 - hMarginScale;
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