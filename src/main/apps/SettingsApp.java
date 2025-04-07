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

        colorPicker = Colors.getBaseColorPicker();

        createUI();
    }

    public void setUIColor(int color) {
        colorPicker.setRGB(color);
    }

    public void setDarkMode(boolean darkMode) {
        if (Colors.isDarkMode() == darkMode) {
            return;
        }
        SVector[] oldColors = Colors.getDefaultColors();
        Colors.setDarkMode(darkMode);
        SVector[] newColors = Colors.getDefaultColors();

        int baseRGB = Colors.getBaseColor();

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