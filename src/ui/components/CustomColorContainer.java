package ui.components;

import java.util.function.Consumer;

import org.lwjglx.util.vector.Vector4f;

import main.ColorArray;
import main.apps.MainApp;
import sutil.ui.UISizes;
import sutil.ui.elements.UIContainer;

public class CustomColorContainer extends UIContainer {

    public CustomColorContainer(ColorArray colors, Consumer<Vector4f> clickAction) {
        super(HORIZONTAL, CENTER);

        for (int i = 0; i < colors.getCapacity(); i++) {
            final int j = i;
            UIColorElement button = new UIColorElement(() -> MainApp.toVector4f(colors.getColor(j)),
                    UISizes.COLOR_BUTTON);
            button.setLeftClickAction(() -> {
                Vector4f color = MainApp.toVector4f(colors.getColor(j));
                if (color != null) {
                    clickAction.accept(color);
                }
            });
            add(button);
        }
    }
}