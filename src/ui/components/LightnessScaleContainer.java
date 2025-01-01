package ui.components;

import main.ColorPicker;
import renderEngine.Window;
import sutil.math.SVector;
import sutil.ui.UIContainer;
import sutil.ui.UIStyle;
import ui.Colors;

public class LightnessScaleContainer extends UIContainer {

    private Slider slider1, slider2;

    private static final UIStyle SLIDER_STYLE = new UIStyle(() -> Colors.getTextColor(), () -> null, () -> 1.0);

    public LightnessScaleContainer(ColorPicker colorPicker) {
        super(HORIZONTAL, TOP);

        setFillSize();
        noOutline();
        zeroMargin().zeroPadding();

        slider1 = new Slider();
        add(slider1);

        LightnessScale lightnessScale = new LightnessScale(colorPicker);
        add(lightnessScale);

        slider2 = new Slider();
        add(slider2);

        colorPicker.setLightnessScale(lightnessScale);
    }

    public class LightnessScale extends UIContainer implements DragTarget {

        private ColorPicker colorPicker;

        public LightnessScale(ColorPicker colorPicker) {
            super(0, 0);
            this.colorPicker = colorPicker;
            setFillSize();
            noOutline();
            setClickAction(() -> colorPicker.setDragTarget(this));
        }

        public double getHue() {
            return colorPicker.getHue();
        }

        public double getSaturation() {
            return colorPicker.getSaturation();
        }

        @Override
        public void drag() {
            Window window = colorPicker.getWindow();
            SVector mousePos = window.getMousePosition();
            SVector absolutePos = getAbsolutePosition();
            mousePos = new SVector(mousePos).sub(absolutePos);
            mousePos.y = Math.min(Math.max(0, mousePos.y), size.y);

            colorPicker.setLightness(1 - mousePos.y / size.y);

            setCursorPosition(new SVector(0, mousePos.y));
        }

        @Override
        public void setCursorPosition(SVector position) {
            slider1.setY(position.y);
            slider2.setY(position.y);
        }

        @Override
        public double getMargin() {
            return 13;
        }
    }

    private class Slider extends UIContainer {

        private static final double WIDTH = 20;

        public Slider() {
            super(VERTICAL, 0);
            noOutline();
            UIContainer child = new UIContainer(0, 0);
            child.zeroMargin().setFixedSize(new SVector(WIDTH, 2));
            child.setStyle(SLIDER_STYLE);
            add(child);

            setFixedSize(new SVector(1, 1).scale(WIDTH));
        }

        @Override
        public void positionChildren() {
        }

        public void setY(double y) {
            SVector pos = children.get(0).getPosition();
            pos.y = y;
        }
    }
}