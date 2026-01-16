package sutil.ui;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class UIRadioButtonList extends UIContainer {

    public UIRadioButtonList(String[] options, Supplier<Integer> stateSupplier, Consumer<Integer> stateConsumer) {
        super(VERTICAL, LEFT);

        noOutline();

        for (int i = 0; i < options.length; i++) {
            UIContainer row = new UIContainer(HORIZONTAL, CENTER);
            row.zeroMargin().noOutline();

            row.add(new UIRadioButton(i, stateSupplier));
            row.add(new UIText(options[i]));

            final int j = i;
            row.setLeftClickAction(() -> stateConsumer.accept(j));

            add(row);
        }
    }
}