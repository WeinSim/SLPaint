package sutil.ui.elements;

import java.util.function.BooleanSupplier;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

import sutil.ui.UI;
import sutil.ui.UIColors;
import sutil.ui.UIShape;
import sutil.ui.UISizes;

public class UIRadioButtonList extends UIContainer {

    public UIRadioButtonList(int orientation, String[] options, IntSupplier stateSupplier, IntConsumer stateConsumer) {
        super(orientation, orientation == UI.VERTICAL ? UI.LEFT : UI.CENTER);

        noOutline();
        zeroMargin();
        setPaddingScale(2.0);

        for (int i = 0; i < options.length; i++) {
            UIContainer row = new UIContainer(UI.HORIZONTAL, UI.CENTER);
            row.zeroMargin().noOutline();

            final int j = i;
            row.setLeftClickAction(() -> stateConsumer.accept(j));
            row.setHandCursor();
            row.setSelectable(true);

            row.add(new UIRadioButton(i, stateSupplier));
            row.add(new UIText(options[i]));

            add(row);
        }
    }

    private static class UIRadioButton extends UIContainer {

        UIRadioButton(int index, IntSupplier stateSupplier) {
            super(UI.VERTICAL, UI.CENTER);

            noOutline();
            zeroMargin();

            style.setBackgroundColor(UIColors.BACKGROUND_2);
            style.setShape(UIShape.ELLIPSE);

            setFixedSize(UISizes.RADIO.getWidthHeight());

            add(new RadioButtonInside(() -> stateSupplier.getAsInt() == index));
        }
    }

    private static class RadioButtonInside extends UIElement {

        RadioButtonInside(BooleanSupplier visibilitySupplier) {
            setVisibilitySupplier(visibilitySupplier);

            style.setBackgroundColor(UIColors.HIGHLIGHT);
            style.setShape(UIShape.ELLIPSE);
        }

        @Override
        public void setPreferredSize() {
            size.set(UISizes.RADIO_INSIDE.getWidthHeight());
        }
    }
}