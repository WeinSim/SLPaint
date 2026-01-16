package sutil.ui;

public class UISeparator extends UIContainer {

    public UISeparator() {
        super(VERTICAL, LEFT);

        style.setStrokeColor(() -> panel.separatorColor());
        zeroMargin();
        zeroPadding();
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
    }
}