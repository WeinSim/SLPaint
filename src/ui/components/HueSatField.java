package ui.components;

import java.util.function.Supplier;

import main.ColorPicker;
import main.apps.App;
import sutil.math.SVector;
import sutil.ui.UIColors;
import sutil.ui.UIContainer;
import sutil.ui.UIDragContainer;
import sutil.ui.UIElement;
import sutil.ui.UIFloatContainer;
import sutil.ui.UISizes;
import sutil.ui.UIStyle;

public class HueSatField extends UIDragContainer {

    /**
     * Relative to slider length
     */
    private static final double CURSOR_LINE_LENGTH = 1;
    private static final double CURSOR_CENTER_GAP = 1;
    /**
     * Relative to slider width
     */
    private static final double CURSOR_WIDTH = 2;

    private ColorPicker colorPicker;

    private double nextX;

    private Supplier<Double> sizeSupplier;

    public HueSatField(ColorPicker colorPicker, Supplier<Double> sizeSupplier) {
        this.colorPicker = colorPicker;
        this.sizeSupplier = sizeSupplier;

        noOutline();

        add(new Cursor(colorPicker));

        nextX = 0;
    }

    @Override
    public void update() {
        super.update();

        double size = sizeSupplier.get();
        setFixedSize(new SVector(size, size));
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

    protected class Cursor extends UIFloatContainer {

        public Cursor(ColorPicker colorPicker) {
            super(0, 0);

            noOutline();

            for (int i = 0; i < 4; i++) {
                add(new CursorLine(i % 2 == 0));
            }

            setFixedSize(new SVector(0, 0));

            clipToRoot = false;
            relativeLayer = 0;
            ignoreClipArea = false;
        }

        @Override
        public void update() {
            super.update();

            clearAnchors();
            SVector pos = new SVector(getRelativeX(), getRelativeY()).mult(parent.getSize());
            addAnchor(Anchor.CENTER_CENTER, pos);
        }

        @Override
        public void positionChildren() {
            double w = panel.get(UISizes.SCALE_SLIDER_WIDTH);
            final double a = w * CURSOR_WIDTH / 2;
            double len = panel.get(UISizes.SCALE_SLIDER_LENGTH);
            final double b = len * (CURSOR_LINE_LENGTH + CURSOR_CENTER_GAP / 2);
            final double c = len * CURSOR_CENTER_GAP / 2;

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
    }

    private static class CursorLine extends UIContainer {

        private boolean vertical;

        public CursorLine(boolean vertical) {
            super(0, 0);

            this.vertical = vertical;

            // clipToRoot = false;

            setStyle(new UIStyle(() -> panel.get(UIColors.TEXT), () -> null, () -> panel.get(UISizes.STROKE_WEIGHT)));
        }

        @Override
        public void update() {
            super.update();

            double len = panel.get(UISizes.MARGIN) * CURSOR_LINE_LENGTH;
            double w = panel.get(UISizes.SCALE_SLIDER_WIDTH) * CURSOR_WIDTH;

            if (vertical) {
                setFixedSize(new SVector(w, len));
            } else {
                setFixedSize(new SVector(len, w));
            }
        }
    }
}