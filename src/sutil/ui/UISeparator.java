package sutil.ui;

import sutil.math.SVector;
import ui.Colors;

public class UISeparator extends UIContainer {

    public UISeparator() {
        super(VERTICAL, LEFT);
        outlineNormal = false;

        add(new UISeparatorInside());
    }

    @Override
    public void update(SVector mouse) {
        if (parent.getOrientation() == VERTICAL) {
            setHFillSize();
            setVMinimalSize();
        } else {
            setHMinimalSize();
            setVFillSize();
        }
        super.update(mouse);
    }

    @Override
    public double getHMargin() {
        return parent.orientation == VERTICAL ? 0 : panel.getMargin();
    }

    @Override
    public double getVMargin() {
        return parent.orientation == VERTICAL ? panel.getMargin() : 0;
    }

    private class UISeparatorInside extends UIContainer {

        public UISeparatorInside() {
            super(VERTICAL, LEFT);
            setHFillSize();
            setVFillSize();
            zeroMargin();

            style.setOutlineColorGetter(() -> Colors.getSeparatorColor());
        }
    }
}