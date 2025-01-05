package main.apps;

import main.ColorButtonArray;
import main.ColorPicker;
import renderEngine.Window;
import sutil.math.SVector;
import ui.Colors;
import ui.SettingsUI;

public final class SettingsApp extends App {

    private ColorButtonArray customColorButtonArray;

    private ColorPicker colorPicker;

    public SettingsApp(MainApp mainApp) {
        super(900, 650, Window.NORMAL, true, "Settings", mainApp);

        customColorButtonArray = new ColorButtonArray(SettingsUI.NUM_UI_BASE_COLOR_BUTTONS);

        colorPicker = new ColorPicker(this, MainApp.toInt(Colors.getBaseColor()),
                (Integer color) -> addCustomColor(color));

        createUI();
    }

    @Override
    public void update(double deltaT) {
        super.update(deltaT);

        colorPicker.update();
        Colors.setBaseColor(MainApp.toSVector(colorPicker.getRGB()));
    }

    public void setUIColor(int color) {
        colorPicker.setRGB(color);
    }

    public void toggleDarkMode() {
        SVector[] oldColors = Colors.getDefaultColors();
        Colors.setDarkMode(!Colors.isDarkMode());
        SVector[] newColors = Colors.getDefaultColors();

        int baseRGB = MainApp.toInt(Colors.getBaseColor());

        // TODO: this is a bit of a hack
        for (int i = 0; i < oldColors.length; i++) {
            if (baseRGB == MainApp.toInt(oldColors[i])) {
                setUIColor(MainApp.toInt(newColors[i]));
            }
        }
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