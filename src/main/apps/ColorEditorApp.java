package main.apps;

import main.ColorPicker;

public final class ColorEditorApp extends App {

    private ColorPicker colorPicker;

    private MainApp mainApp;

    public ColorEditorApp(MainApp mainApp, int initialColor) {
        super(500, 500, 0, false, "Color Editor", mainApp);
        this.mainApp = mainApp;

        colorPicker = new ColorPicker(initialColor);

        adjustSizeOnInit = true;

        createUI();
    }

    public ColorPicker getColorPicker() {
        return colorPicker;
    }

    public MainApp getMainApp() {
        return mainApp;
    }

    @Override
    protected App createChildApp(int dialogType) {
        return null;
    }
}