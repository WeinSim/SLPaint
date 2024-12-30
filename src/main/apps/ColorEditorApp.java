package main.apps;

import main.ColorPicker;
import sutil.math.SVector;
import sutil.ui.UIRoot;

public final class ColorEditorApp extends App {

    private ColorPicker colorPicker;

    public ColorEditorApp(MainApp mainApp, int initialColor) {
        super(500, 500, 0, false, "Color Editor", mainApp);

        colorPicker = new ColorPicker(this, initialColor, (Integer color) -> {
            mainApp.addCustomColor(color);
            window.requestClose();
        });

        createUI();

        UIRoot root = ui.getRoot();
        SVector rootSize = root.getSize();
        int width = (int) rootSize.x;
        int height = (int) rootSize.y;
        window.setSizeAndCenter(width, height);
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