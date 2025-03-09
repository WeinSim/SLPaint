package ui.components;

import main.ColorPicker;
import main.apps.App;
import sutil.math.SVector;
import sutil.ui.Draggable;
import sutil.ui.UIContainer;
import sutil.ui.UIDragContainer;
import sutil.ui.UIElement;
import sutil.ui.UIStyle;
import ui.Colors;
import ui.Sizes;

public class HueSatField extends UIDragContainer<HueSatField.Cursor> {

    private static final double CURSOR_LINE_LENGTH = 10;
    private static final double CURSOR_LINE_WIDTH = 4;
    private static final double CURSOR_CENTER_GAP = 10;

    public HueSatField(ColorPicker colorPicker, double size) {
        super(new Cursor(colorPicker));

        noOutline();
        setFixedSize(new SVector(size, size));
    }

    @Override
    public void positionChildren() {
        super.positionChildren();

        ((Cursor) children.get(0)).positionChildren();
    }

    protected static class Cursor extends UIContainer implements Draggable {

        private ColorPicker colorPicker;

        private double nextX;

        public Cursor(ColorPicker colorPicker) {
            super(0, 0);

            this.colorPicker = colorPicker;
            noOutline();

            for (int i = 0; i < 4; i++) {
                add(new CursorLine(i % 2 == 0));
            }

            setFixedSize(new SVector(0, 0));

            nextX = 0;
        }

        @Override
        public void positionChildren() {
            final double a = CURSOR_LINE_WIDTH / 2;
            final double b = CURSOR_LINE_LENGTH + CURSOR_CENTER_GAP / 2;
            final double c = CURSOR_CENTER_GAP / 2;
            children.get(0).getPosition().set(-a, -b);
            children.get(1).getPosition().set(c, -a);
            children.get(2).getPosition().set(-a, c);
            children.get(3).getPosition().set(-b, -a);
        }

        @Override
        public double getRelativeX() {
            if (App.isCircularHueSatField()) {
                double radius = colorPicker.getSaturation();
                double angle = colorPicker.getHue() / 180 * Math.PI;
                return Math.cos(angle) * radius / 2 + 0.5;
            } else {
                return colorPicker.getHue() / 360.0;
            }
        }

        @Override
        public double getRelativeY() {
            if (App.isCircularHueSatField()) {
                double radius = colorPicker.getSaturation();
                double angle = colorPicker.getHue() / 180 * Math.PI;
                return Math.sin(angle) * radius / 2 + 0.5;
            } else {
                return 1 - colorPicker.getSaturation();
            }
        }

        @Override
        public void setRelativeX(double x) {
            if (App.isCircularHueSatField()) {
                nextX = x;
            } else {
                x = Math.min(Math.max(0, x), 1);
                colorPicker.setHue(x * 360.0);
            }
        }

        @Override
        public void setRelativeY(double y) {
            if (App.isCircularHueSatField()) {
                y -= 0.5;
                double x = nextX - 0.5;
                double angle = Math.atan2(y, x) / Math.PI * 180;
                colorPicker.setHue(angle);
                colorPicker.setSaturation(Math.min(1, 2 * Math.sqrt(x * x + y * y)));
            } else {
                y = Math.min(Math.max(0, y), 1);
                colorPicker.setSaturation(1 - y);
            }
        }
    }

    private static class CursorLine extends UIElement {

        private boolean vertical;

        public CursorLine(boolean vertical) {
            this.vertical = vertical;

            setStyle(new UIStyle(() -> Colors.getTextColor(), () -> null, () -> Sizes.STROKE_WEIGHT.size));
        }

        @Override
        public void setMinSize() {
            if (vertical) {
                size.set(CURSOR_LINE_WIDTH, CURSOR_LINE_LENGTH);
            } else {
                size.set(CURSOR_LINE_LENGTH, CURSOR_LINE_WIDTH);
            }
        }
    }
}