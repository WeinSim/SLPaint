package sutil.ui;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class UIRadioButtonList extends UIContainer {

    public UIRadioButtonList(int orientation, String[] options, IntSupplier stateSupplier, IntConsumer stateConsumer) {
        super(orientation, orientation == UI.VERTICAL ? UI.LEFT : UI.CENTER);

        noOutline();
        zeroMargin();

        for (int i = 0; i < options.length; i++) {
            UIContainer row = new UIContainer(UI.HORIZONTAL, UI.CENTER);
            row.zeroMargin().noOutline().setHandCursor();

            row.add(new UIRadioButton(i, stateSupplier));
            row.add(new UIText(options[i]));

            final int j = i;
            row.setLeftClickAction(() -> stateConsumer.accept(j));

            row.setSelectable(true);

            add(row);
        }
    }
}