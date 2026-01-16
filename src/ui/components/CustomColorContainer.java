package ui.components;

import java.util.function.Consumer;

import main.ColorArray;
import sutil.ui.UIContainer;
import ui.AppUI;
import ui.Sizes;

public class CustomColorContainer extends UIContainer {

    public CustomColorContainer(ColorArray colors, Consumer<Integer> clickAction) {
        super(HORIZONTAL, CENTER);

        for (int i = 0; i < colors.getCapacity(); i++) {
            final int j = i;
            UIColorElement button = new UIColorElement(
                    () -> colors.getColor(j),
                    () -> ((AppUI<?>) panel).getSize(Sizes.COLOR_BUTTON));
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