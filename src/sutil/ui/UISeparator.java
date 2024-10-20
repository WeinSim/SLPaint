package sutil.ui;

import main.MainApp;
import sutil.math.SVector;

public class UISeparator extends UIContainer {

    public UISeparator() {
        super(VERTICAL, LEFT);
        outlineNormal = false;

        setFillSize();

        add(new UISeparatorInside());
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
            add(new UIEmpty(1));
            setZeroMargin(true);

            UIGetter<SVector> bgColor = () -> MainApp.SEPARATOR_COLOR;
            UIGetter<SVector> olColor = () -> null;
            UIGetter<Double> swGetter = () -> 1.0;
            setStyle(new UIStyle(bgColor, olColor, swGetter));
        }
    }
}