package ui.components;

import sutil.math.SVector;
import sutil.ui.UIContainer;
import sutil.ui.UIStyle;
import ui.Colors;
import ui.Sizes;

public class UIScaleContainer extends UIContainer {

    private Slider slider1, slider2;

    protected UIScale scale;

    private static final UIStyle SLIDER_STYLE = new UIStyle(
            () -> Colors.getTextColor(),
            () -> null,
            () -> Sizes.STROKE_WEIGHT.size);

    public UIScaleContainer(UIScale scale) {
        super(1 - scale.getOrientation(), TOP);

        setFillSize();
        noOutline();
        zeroMargin().zeroPadding();

        slider1 = new Slider();
        add(slider1);

        // scale = createScale();
        this.scale = scale;
        scale.setParent(this);
        add(scale);

        slider2 = new Slider();
        add(slider2);
    }

    public void setSliderPosition(double coord) {
        slider1.setCoord(coord);
        slider2.setCoord(coord);
    }

    private class Slider extends UIContainer {

        private static final double WIDTH = 20;

        public Slider() {
            super(UIScaleContainer.this.orientation, 0);
            noOutline();
            UIContainer child = new UIContainer(0, 0);
            child.zeroMargin().setFixedSize(
                    orientation == HORIZONTAL
                            ? new SVector(WIDTH, 2)
                            : new SVector(2, WIDTH));
            child.setStyle(SLIDER_STYLE);
            add(child);

            setFixedSize(new SVector(1, 1).scale(WIDTH));
        }

        @Override
        public void positionChildren() {
        }

        public void setCoord(double coord) {
            SVector pos = children.get(0).getPosition();
            if (orientation == HORIZONTAL) {
                pos.y = coord;
            } else {
                pos.x = coord;
            }
        }
    }
}