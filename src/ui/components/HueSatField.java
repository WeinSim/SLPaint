package ui.components;

import java.util.function.Supplier;

import main.ColorPicker;
import main.apps.App;
import sutil.math.SVector;
import sutil.ui.Draggable;
import sutil.ui.UIContainer;
import sutil.ui.UIDragContainer;
import sutil.ui.UIElement;
import sutil.ui.UIStyle;
import ui.Colors;

public class HueSatField extends UIDragContainer<HueSatField.Cursor> {

    /**
     * Relative to the margin size
     */
    private static final double CURSOR_LINE_LENGTH = 1;
    private static final double CURSOR_LINE_WIDTH = 0.4;
    private static final double CURSOR_CENTER_GAP = 1;

    private Supplier<Double> sizeSupplier;

    public HueSatField(ColorPicker colorPicker, Supplier<Double> sizeSupplier) {
        super(new Cursor(colorPicker));

        this.sizeSupplier = sizeSupplier;

        noOutline();
    }

    @Override
    public void update() {
        super.update();

        double size = sizeSupplier.get();
        setFixedSize(new SVector(size, size));
    }

    @Override
    public void positionChildren() {
        super.positionChildren();

        draggable.positionChildren();
    }

    @Override
    protected boolean calculateMouseAbove(SVector mouse) {
        if (App.isCircularHueSatField()) {
            double x = (mouse.x - position.x) / size.x - 0.5,
                    y = (mouse.y - position.y) / size.y - 0.5;
            return x * x + y * y < 0.25;
        } else {
            return super.calculateMouseAbove(mouse);
        }
    }

    protected static class Cursor extends Draggable {

        private ColorPicker colorPicker;

        private double nextX;

        public Cursor(ColorPicker colorPicker) {
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
            double margin = panel.marginSize();
            final double a = margin * CURSOR_LINE_WIDTH / 2;
            final double b = margin * (CURSOR_LINE_LENGTH + CURSOR_CENTER_GAP / 2);
            final double c = margin * CURSOR_CENTER_GAP / 2;

            int i = 0;
            for (UIElement child : getChildren()) {
                child.getPosition().set(switch (i++) {
                    case 0 -> new SVector(-a, -b);
                    case 1 -> new SVector(c, -a);
                    case 2 -> new SVector(-a, c);
                    case 3 -> new SVector(-b, -a);
                    default -> null;
                });
            }
        }

        @Override
        public double getRelativeX() {
            if (App.isCircularHueSatField()) {
                double radius = App.isHSLColorSpace() ? colorPicker.getHSLSaturation() : colorPicker.getHSVSaturation();
                double angle = colorPicker.getHue() / 180 * Math.PI;
                return Math.cos(angle) * radius / 2 + 0.5;
            } else {
                return colorPicker.getHue() / 360.0;
            }
        }

        @Override
        public double getRelativeY() {
            double saturation = App.isHSLColorSpace() ? colorPicker.getHSLSaturation() : colorPicker.getHSVSaturation();
            if (App.isCircularHueSatField()) {
                double radius = saturation;
                double angle = colorPicker.getHue() / 180 * Math.PI;
                return Math.sin(angle) * radius / 2 + 0.5;
            } else {
                return 1 - saturation;
            }
        }

        @Override
        public void setRelativeX(double x) {
            if (App.isCircularHueSatField()) {
                nextX = x;
            } else {
                x = Math.min(Math.max(0, x), 1);
                if (App.isHSLColorSpace()) {
                    colorPicker.setHSLHue(x * 360.0);
                } else {
                    colorPicker.setHSVHue(x * 360.0);
                }
            }
        }

        @Override
        public void setRelativeY(double y) {
            if (App.isCircularHueSatField()) {
                y -= 0.5;
                double x = nextX - 0.5;
                double angle = Math.atan2(y, x) / Math.PI * 180;
                angle = (angle + 360) % 360;
                if (App.isHSLColorSpace()) {
                    colorPicker.setHSLHue(angle);
                    colorPicker.setHSLSaturation(Math.min(1, 2 * Math.sqrt(x * x + y * y)));
                } else {
                    colorPicker.setHSVHue(angle);
                    colorPicker.setHSVSaturation(Math.min(1, 2 * Math.sqrt(x * x + y * y)));
                }
            } else {
                y = Math.min(Math.max(0, y), 1);
                if (App.isHSLColorSpace()) {
                    colorPicker.setHSLSaturation(1 - y);
                } else {
                    colorPicker.setHSVSaturation(1 - y);
                }
            }
        }
    }

    private static class CursorLine extends UIContainer {

        private boolean vertical;

        public CursorLine(boolean vertical) {
            super(0, 0);

            this.vertical = vertical;

            // clipToRoot = false;

            setStyle(new UIStyle(Colors::text, () -> null, () -> panel.strokeWeightSize()));
        }

        @Override
        public void update() {
            super.update();

            double margin = panel.marginSize();

            if (vertical) {
                setFixedSize(new SVector(CURSOR_LINE_WIDTH, CURSOR_LINE_LENGTH).scale(margin));
            } else {
                setFixedSize(new SVector(CURSOR_LINE_LENGTH, CURSOR_LINE_WIDTH).scale(margin));
            }
        }
    }
}