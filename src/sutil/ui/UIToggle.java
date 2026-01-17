package sutil.ui;

import java.util.function.Consumer;
import java.util.function.Supplier;

import sutil.math.SVector;

public class UIToggle extends UIContainer {

    private Supplier<Boolean> stateSupplier;

    public UIToggle(Supplier<Boolean> stateSupplier, Consumer<Boolean> stateConsumer) {
        this.stateSupplier = stateSupplier;
        super(HORIZONTAL, LEFT);

        setLeftClickAction(() -> stateConsumer.accept(!stateSupplier.get()));

        noOutline();
        double yDiff = UISizes.RADIO.get() - UISizes.RADIO_INSIDE.get();
        double margin = UISizes.MARGIN.get();
        setMarginScale(yDiff / 2 / margin);

        style.setBackgroundColor(UIColors.BACKGROUND_HIGHLIGHT_2);
        style.setShape(UIShape.ROUND_RECTANGLE);

        setFixedSize(new SVector(UISizes.TOGGLE_WIDTH.get(), UISizes.RADIO.get()));

        add(new ToggleInside());
    }

    @Override
    public void update() {
        super.update();

        setAlignment(getState() ? RIGHT : LEFT, CENTER);
    }

    private boolean getState() {
        return stateSupplier.get();
    }

    private class ToggleInside extends UIElement {

        ToggleInside() {
            style.setShape(UIShape.ELLIPSE);
            style.setBackgroundColor(UIColors.TEXT);

            size.set(UISizes.RADIO_INSIDE.getWidthHeight());
        }

        @Override
        public void setPreferredSize() {
        }
    }
}