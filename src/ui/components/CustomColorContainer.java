package ui.components;

import main.ColorButtonArray;
import sutil.ui.UIContainer;
import sutil.ui.UISetter;
import ui.MainUI;

public class CustomColorContainer extends UIContainer {

    public CustomColorContainer(ColorButtonArray colors, UISetter<Integer> clickAction) {
        super(HORIZONTAL, CENTER);

        for (int i = 0; i < colors.getLength(); i++) {
            final int j = i;
            UIColorElement button = new UIColorElement(() -> colors.getColor(j), MainUI.COLOR_BUTTON_SIZE, true);
            button.setClickAction(() -> clickAction.set(colors.getColor(j)));
            add(button);
        }
    }
}