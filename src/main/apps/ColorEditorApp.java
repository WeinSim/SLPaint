package main.apps;

import main.ColorPicker;

public final class ColorEditorApp extends App {

    private ColorPicker colorPicker;

    public ColorEditorApp(MainApp mainApp, int initialColor) {
        super(500, 500, 0, false, "Color Editor", mainApp);

        colorPicker = new ColorPicker(this, initialColor, (Integer color) -> {
            mainApp.addCustomColor(color);
            window.requestClose();
        });

        adjustSizeOnInit = true;

        createUI();
    }

    @Override
    public void update(double deltaT) {
        super.update(deltaT);

        colorPicker.update();
    }

    public ColorPicker getColorPicker() {
        return colorPicker;
    }

    @Override
    protected App createChildApp(int dialogType) {
        return null;
    }
}