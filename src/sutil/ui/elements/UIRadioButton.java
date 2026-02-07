package sutil.ui.elements;

import java.util.function.IntSupplier;

import sutil.ui.UI;
import sutil.ui.UIColors;
import sutil.ui.UIShape;
import sutil.ui.UISizes;

public class UIRadioButton extends UIContainer {

    private IntSupplier stateSupplier;
    private int index;

    public UIRadioButton(int index, IntSupplier stateSupplier) {
        super(UI.VERTICAL, UI.CENTER);
        this.index = index;
        this.stateSupplier = stateSupplier;

        noOutline();
        zeroMargin();

        style.setBackgroundColor(UIColors.BACKGROUND_2);
        style.setShape(UIShape.ELLIPSE);

        setFixedSize(UISizes.RADIO.getWidthHeight());

        add(new RadioButtonInside());
    }

    public boolean getState() {
        return stateSupplier.getAsInt() == index;
    }

    private class RadioButtonInside extends UIElement {

        RadioButtonInside() {
            setVisibilitySupplier(UIRadioButton.this::getState);

            style.setBackgroundColor(UIColors.HIGHLIGHT);
            style.setShape(UIShape.ELLIPSE);

            size.set(UISizes.RADIO_INSIDE.getWidthHeight());
        }

        @Override
        public void setPreferredSize() {
        }
    }
}