package ui.components;

import main.ColorArray;
import sutil.ui.UIContainer;
import sutil.ui.UISetter;
import ui.Sizes;

public class CustomColorContainer extends UIContainer {

    public CustomColorContainer(ColorArray colors, UISetter<Integer> clickAction) {
        super(HORIZONTAL, CENTER);

        for (int i = 0; i < colors.getLength(); i++) {
            final int j = i;
            UIColorElement button = new UIColorElement(() -> colors.getColor(j), Sizes.COLOR_BUTTON.size, true);
            button.setClickAction(() -> clickAction.set(colors.getColor(j)));
            add(button);
        }
    }
}