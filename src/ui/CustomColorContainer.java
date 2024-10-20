package ui;

import java.util.ArrayList;

import main.MainApp;
import sutil.ui.UIContainer;
import ui.components.ColorButton;

public class CustomColorContainer extends UIContainer {

    private MainApp app;

    public CustomColorContainer(int orientation, int alignment, MainApp app) {
        super(orientation, alignment);
        this.app = app;
        app.setCustomColorContainer(this);
    }
    
    public void updateColors(ArrayList<Integer> colors) {
        children.clear();

        for (int i = 0; i < MainUI.NUM_COLOR_BUTTONS_PER_ROW; i++) {
            Integer color = i < colors.size() ? colors.get(i) : null;
            ColorButton button = new ColorButton(() -> color, MainUI.COLOR_BUTTON_SIZE);
            if (color != null) {
                button.setClickAction(() -> app.selectColor(color));
            }
            add(button);
        }
    }
}