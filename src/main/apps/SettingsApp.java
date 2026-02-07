package main.apps;

import org.lwjgl.glfw.GLFW;
import org.lwjglx.util.vector.Vector4f;

import main.ColorPicker;
import renderengine.Window;
import ui.AppUI;
import ui.SettingsUI;

public final class SettingsApp extends App {

    private ColorPicker colorPicker;

    public SettingsApp(MainApp mainApp) {
        super(900, 650, Window.NORMAL, true, false, "Settings", mainApp);

        colorPicker = AppUI.getBaseColorPicker();

        addKeyboardShortcut("close", GLFW.GLFW_KEY_ESCAPE, 0, window::requestClose, false);

        loadUI();
    }

    public void setUIColor(int color) {
        colorPicker.setRGB(color);
    }

    public void setDarkMode(boolean darkMode) {
        if (AppUI.isDarkMode() == darkMode)
            return;

        Vector4f[] oldColors = AppUI.getDefaultUIColors();
        AppUI.setDarkMode(darkMode);
        Vector4f[] newColors = AppUI.getDefaultUIColors();

        int baseRGB = MainApp.toInt(AppUI.getBaseColor());

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

    @Override
    protected AppUI<?> createUI() {
        return new SettingsUI(this);
    }
}