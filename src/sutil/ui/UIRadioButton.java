package sutil.ui;

import java.util.function.Supplier;

public class UIRadioButton extends UIContainer {

    private Supplier<Integer> stateSupplier;
    private int index;

    public UIRadioButton(int index, Supplier<Integer> stateSupplier) {
        super(VERTICAL, CENTER);
        this.index = index;
        this.stateSupplier = stateSupplier;

        noOutline();
        zeroMargin();

        style.setBackgroundColor(UIColors.BACKGROUND_HIGHLIGHT_2);
        style.setShape(UIShape.ELLIPSE);

        setFixedSize(UISizes.RADIO.getWidthHeight());

        add(new RadioButtonInside());
    }

    public boolean getState() {
        return stateSupplier.get() == index;
    }

    private class RadioButtonInside extends UIElement {

        RadioButtonInside() {
            setVisibilitySupplier(UIRadioButton.this::getState);

            style.setBackgroundColor(UIColors.TEXT);
            style.setShape(UIShape.ELLIPSE);

            size.set(UISizes.RADIO_INSIDE.getWidthHeight());
        }

        @Override
        public void setPreferredSize() {
        }
    }
}