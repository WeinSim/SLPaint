package ui;

import org.lwjglx.util.vector.Vector4f;

import main.ColorPicker;
import main.apps.MainApp;
import main.settings.BooleanSetting;
import main.settings.ColorSetting;

public class Colors {

    private static final Vector4f[] DEFAULT_UI_COLORS_DARK = {
            new Vector4f(0.3f, 0.3f, 0.3f, 1.0f),
            new Vector4f(0.07f, 0.35f, 0.5f, 1.0f),
            new Vector4f(0.5f, 0.07f, 0.35f, 1.0f),
            new Vector4f(0.42f, 0.14f, 0.14f, 1.0f)
    };

    private static final Vector4f[] DEFAULT_UI_COLORS_LIGHT = {
            new Vector4f(1, 1, 1, 1),
            new Vector4f(0.67f, 0.85f, 0.95f, 1.0f),
            new Vector4f(0.95f, 0.63f, 0.84f, 1.0f),
            new Vector4f(0.84f, 0.51f, 0.51f, 1.0f)
    };

    private static enum UIColor {
        BACKGROUND_NORMAL(0.24, 0.95),
        BACKGROUND_HIGHLIGHT(0.5, 0.9),
        BACKGROUND_HIGHLIGHT_2(0.7, 0.85),
        OUTLINE_NORMAL(1.1, 0.45),
        OUTLINE_HIGHLIGHT(0.9, 0.6),
        SEPARATOR(0.56, 0.7),
        CANVAS(0.4, 0.85),
        TRANSPARENCY_1(0.6, 1),
        TRANSPARENCY_2(0.0, 0.85);

        public final double darkModeBrightness, lightModeBrightness;

        private UIColor(double darkModeBrightness, double lightModeBrightness) {
            this.darkModeBrightness = darkModeBrightness;
            this.lightModeBrightness = lightModeBrightness;
        }
    }

    private static BooleanSetting darkMode = new BooleanSetting("darkMode");
    private static ColorSetting baseColor = new ColorSetting("baseColor");

    private Colors() {
    }

    public static Vector4f[] defaults() {
        return isDarkMode() ? DEFAULT_UI_COLORS_DARK : DEFAULT_UI_COLORS_LIGHT;
    }

    public static int numDefaults() {
        return DEFAULT_UI_COLORS_DARK.length;
    }

    public static boolean isDarkMode() {
        return darkMode.get();
    }

    public static void setDarkMode(boolean darkMode) {
        Colors.darkMode.set(darkMode);
    }

    public static int baseColor() {
        return baseColor.get().getRGB();
    }

    public static ColorPicker getBaseColorPicker() {
        return baseColor.get();
    }

    public static Vector4f backgroundNormal() {
        return getUIColor(UIColor.BACKGROUND_NORMAL);
    }

    public static Vector4f backgroundHighlight() {
        return getUIColor(UIColor.BACKGROUND_HIGHLIGHT);
    }

    public static Vector4f backgroundHighlight2() {
        return getUIColor(UIColor.BACKGROUND_HIGHLIGHT_2);
    }

    public static Vector4f outlineNormal() {
        return getUIColor(UIColor.OUTLINE_NORMAL);
    }

    public static Vector4f outlineHighlight() {
        return getUIColor(UIColor.OUTLINE_HIGHLIGHT);
    }

    public static Vector4f separator() {
        return getUIColor(UIColor.SEPARATOR);
    }

    public static Vector4f canvas() {
        return getUIColor(UIColor.CANVAS);
    }

    public static Vector4f[] transparent() {
        return new Vector4f[] { getUIColor(UIColor.TRANSPARENCY_1), getUIColor(UIColor.TRANSPARENCY_2) };
    }

    public static Vector4f[] selectionBorder() {
        return new Vector4f[] { new Vector4f(0, 0, 0, 1), new Vector4f(1, 1, 1, 1) };
    }

    public static Vector4f text() {
        return isDarkMode() ? new Vector4f(1, 1, 1, 1) : new Vector4f(0, 0, 0, 1);
    }

    private static Vector4f getUIColor(UIColor colorType) {
        double brightness = isDarkMode() ? colorType.darkModeBrightness : colorType.lightModeBrightness;

        brightness = switch (colorType) {
            // case TRANSPARENCY_1 -> 0.6;
            // case TRANSPARENCY_2 -> 0.0;
            default -> brightness;
        };

        // This method is being called ~5520 times per second (which is too much!)
        // Maybe cache the results?

        Vector4f ret = (Vector4f) MainApp.toVector4f(baseColor()).scale((float) brightness);
        ret.w = 1.0f;
        return ret;
    }
}