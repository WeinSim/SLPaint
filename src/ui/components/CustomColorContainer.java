package ui.components;

import main.ColorArray;
import java.util.function.Consumer;

import sutil.ui.UIContainer;
import ui.Sizes;

public class CustomColorContainer extends UIContainer {

    public CustomColorContainer(ColorArray colors, Consumer<Integer> clickAction) {
        super(HORIZONTAL, CENTER);

        for (int i = 0; i < colors.getCapacity(); i++) {
            final int j = i;
            UIColorElement button = new UIColorElement(() -> colors.getColor(j), Sizes.COLOR_BUTTON.size, true);
            button.setLeftClickAction(() -> {
                Integer color = colors.getColor(j);
                if (color != null) {
                    clickAction.accept(color);
                }
            });
            add(button);
        }
    }
}