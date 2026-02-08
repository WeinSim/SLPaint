package sutil.ui.elements;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import sutil.math.SVector;
import sutil.ui.UIColors;
import sutil.ui.UIShape;
import sutil.ui.UISizes;

public class UIToggleList extends UIContainer {

    public UIToggleList(String label, BooleanSupplier supplier, Consumer<Boolean> consumer) {
        this();

        addToggle(label, supplier, consumer);
    }

    public UIToggleList() {
        super(VERTICAL, 0);

        zeroMargin();
        noOutline();
    }

    public void addToggle(String label, BooleanSupplier supplier, Consumer<Boolean> consumer) {
        UIContainer container = new UIContainer(HORIZONTAL, CENTER);
        container.setHFillSize().zeroMargin().noOutline();

        container.setLeftClickAction(() -> consumer.accept(!supplier.getAsBoolean()));
        container.setHandCursor();
        container.setSelectable(true);

        container.add(new UIText(label));
        container.add(new UIContainer(0, 0).setVMarginScale(0).setHFillSize().noOutline());
        container.add(new UIToggle(supplier));

        add(container);
    }

    private static class UIToggle extends UIContainer {

        BooleanSupplier stateSupplier;

        UIToggle(BooleanSupplier stateSupplier) {
            super(HORIZONTAL, LEFT);
            this.stateSupplier = stateSupplier;

            noOutline();
            double yDiff = UISizes.RADIO.get() - UISizes.RADIO_INSIDE.get();
            double margin = UISizes.MARGIN.get();
            setMarginScale(yDiff / 2 / margin);

            style.setBackgroundColor(UIColors.BACKGROUND_2);
            style.setShape(UIShape.ROUND_RECTANGLE);

            setHandCursor();

            setFixedSize(new SVector(UISizes.TOGGLE_WIDTH.get(), UISizes.RADIO.get()));

            add(new ToggleInside());
        }

        @Override
        public void update() {
            super.update();

            setAlignment(stateSupplier.getAsBoolean() ? RIGHT : LEFT, CENTER);
        }
    }

    private static class ToggleInside extends UIElement {

        ToggleInside() {
            style.setShape(UIShape.ELLIPSE);
            style.setBackgroundColor(UIColors.HIGHLIGHT);

            size.set(UISizes.RADIO_INSIDE.getWidthHeight());
        }

        @Override
        public void setPreferredSize() {
        }
    }
}