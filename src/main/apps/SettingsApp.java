package main.apps;

import main.ColorButtonArray;
import main.ColorPicker;
import renderEngine.Window;
import ui.SettingsUI;

public final class SettingsApp extends App {

    private ColorButtonArray customColorButtonArray;

    private ColorPicker colorPicker;

    public SettingsApp(MainApp mainApp) {
        super(900, 500, Window.NORMAL, true, "Settings", mainApp);

        customColorButtonArray = new ColorButtonArray(SettingsUI.NUM_UI_BASE_COLOR_BUTTONS);

        colorPicker = new ColorPicker(this, MainApp.toInt(App.getBaseColor()),
                (Integer color) -> addCustomColor(color));

        createUI();
    }

    @Override
    public void update(double deltaT) {
        super.update(deltaT);

        colorPicker.update();
        App.setBaseColor(MainApp.toSVector(colorPicker.getRGB()));
    }

    public void setUIColor(int color) {
        colorPicker.setRGB(color);
    }

    public ColorButtonArray getCustomColorButtonArray() {
        return customColorButtonArray;
    }

    public void addCustomColor(Integer color) {
        customColorButtonArray.addColor(color);
        // App.setBaseColor(MainApp.toSVector(color));
    }

    @Override
    protected App createChildApp(int dialogType) {
        return null;
    }

    public ColorPicker getColorPicker() {
        return colorPicker;
    }
}