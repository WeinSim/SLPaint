package ui;

import main.apps.ColorEditorApp;
import sutil.ui.UIContainer;
import sutil.ui.UISizes;
import ui.components.ColorPickContainer;

public class ColorEditorUI extends AppUI<ColorEditorApp> {

    public ColorEditorUI(ColorEditorApp app) {
        super(app);
    }

    @Override
    protected void init() {
        root.setMinimalSize();
        root.setMarginScale(1);

        root.add(new ColorPickContainer(
                app.getColorPicker(),
                color -> {
                    app.getMainApp().addCustomColor(color);
                    app.getWindow().requestClose();
                },
                () -> get(UISizes.COLOR_PICKER_PANEL),
                UIContainer.VERTICAL,
                true,
                true));
    }
}