package main.apps;

import main.ColorPicker;
import renderEngine.Window;
import sutil.math.SVector;
import ui.Colors;
import ui.Sizes;

public final class SettingsApp extends App {

    private ColorPicker colorPicker;

    public SettingsApp(MainApp mainApp) {
        super((int) Sizes.SETTINGS_APP.width, (int) Sizes.SETTINGS_APP.height, Window.NORMAL, true, "Settings",
                mainApp);

        colorPicker = new ColorPicker(this, MainApp.toInt(Colors.getBaseColor()),
                (Integer color) -> MainApp.addCustomUIBaseColor(color));

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

        // this is a bit of a hack
        for (int i = 0; i < oldColors.length; i++) {
            if (baseRGB == MainApp.toInt(oldColors[i])) {
                setUIColor(MainApp.toInt(newColors[i]));
            }
        }
    }

    @Override
    protected App createChildApp(int dialogType) {
        return null;
    }

    public ColorPicker getColorPicker() {
        return colorPicker;
    }
}