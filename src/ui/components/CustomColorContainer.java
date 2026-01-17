package ui.components;

import java.util.function.Consumer;

import main.ColorArray;
import sutil.ui.UIContainer;
import sutil.ui.UISizes;

public class CustomColorContainer extends UIContainer {

    public CustomColorContainer(ColorArray colors, Consumer<Integer> clickAction) {
        super(HORIZONTAL, CENTER);

        for (int i = 0; i < colors.getCapacity(); i++) {
            final int j = i;
            UIColorElement button = new UIColorElement(() -> colors.getColor(j), UISizes.COLOR_BUTTON);
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