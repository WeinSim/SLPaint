package sutil.ui;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import sutil.math.SVector;

public class UIToggle extends UIContainer {

    private BooleanSupplier stateSupplier;

    public UIToggle(BooleanSupplier stateSupplier, Consumer<Boolean> stateConsumer) {
        super(HORIZONTAL, LEFT);
        this.stateSupplier = stateSupplier;

        setLeftClickAction(() -> stateConsumer.accept(!stateSupplier.getAsBoolean()));

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

        setAlignment(getState() ? RIGHT : LEFT, CENTER);
    }

    private boolean getState() {
        return stateSupplier.getAsBoolean();
    }

    private class ToggleInside extends UIElement {

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