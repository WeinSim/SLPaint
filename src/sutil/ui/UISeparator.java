package sutil.ui;

import sutil.math.SVector;
import ui.Colors;

public class UISeparator extends UIContainer {

    public UISeparator() {
        super(VERTICAL, LEFT);
        outlineNormal = false;

        setFillSize();

        zeroMargin();

        add(new UISeparatorMargin());
        add(new UISeparatorInside());
        add(new UISeparatorMargin());
    }

    @Override
    public void update(SVector mouse) {
        orientation = parent.getOrientation();
        super.update(mouse);
    }

    private class UISeparatorInside extends UIContainer {

        public UISeparatorInside() {
            super(VERTICAL, LEFT);
            setFillSize();
            zeroMargin();

            style.setOutlineColorGetter(() -> Colors.getSeparatorColor());
        }
    }

    private class UISeparatorMargin extends UIElement {

        @Override
        public void setMinSize() {
            size.set(getMargin(), 0);
        }
    }
}