package ui.components;

import main.ColorPicker;
import renderEngine.Window;
import sutil.math.SVector;
import sutil.ui.UIContainer;
import sutil.ui.UIElement;
import sutil.ui.UIStyle;

public class HueSatField extends UIContainer implements DragTarget {

    private static final double CURSOR_LINE_LENGTH = 10;
    private static final double CURSOR_LINE_WIDTH = 4;
    private static final double CURSOR_CENTER_GAP = 10;

    private ColorPicker colorPicker;

    public HueSatField(ColorPicker colorPicker, double size) {
        super(0, 0);
        this.colorPicker = colorPicker;

        noOutline();
        setFixedSize(new SVector(size, size));

        add(new Cursor());

        setClickAction(() -> colorPicker.setDragTarget(this));

        colorPicker.setHueSatField(this);
    }

    @Override
    public void drag() {
        Window window = colorPicker.getWindow();
        SVector mousePos = window.getMousePosition();
        SVector absolutePos = getAbsolutePosition();
        mousePos = new SVector(mousePos).sub(absolutePos);
        mousePos.x = Math.min(Math.max(0, mousePos.x), size.x);
        mousePos.y = Math.min(Math.max(0, mousePos.y), size.y);

        colorPicker.setHue(mousePos.x / size.x * 360);
        colorPicker.setSaturation(1 - mousePos.y / size.y);
    }

    @Override
    public void updateCursorPosition() {
        double hue = colorPicker.getHue(),
                saturation = colorPicker.getSaturation();
        SVector pos = new SVector(hue / 360 * size.x, (1 - saturation) * size.y);
        children.get(0).getPosition().set(pos);
    }

    @Override
    public void positionChildren() {

        ((UIContainer) children.get(0)).positionChildren();
    }

    private class Cursor extends UIContainer {

        public Cursor() {
            super(0, 0);
            noOutline();

            for (int i = 0; i < 4; i++) {
                add(new CursorLine(i % 2 == 0));
            }

            // size.set(1, 1);
            // size.scale(2 * CURSOR_LINE_LENGTH + CURSOR_CENTER_GAP);
            // setFixedSize(size);
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
    }

    private class CursorLine extends UIElement {

        private boolean vertical;

        public CursorLine(boolean vertical) {
            this.vertical = vertical;

            setStyle(new UIStyle(() -> new SVector(), () -> null, () -> 1.0));
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