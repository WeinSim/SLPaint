package ui;

import sutil.math.SVector;

public class Colors {

    private static final SVector[] DEFAULT_UI_COLORS_DARK = {
            new SVector(0.3, 0.3, 0.3),
            new SVector(0.07, 0.35, 0.5),
            new SVector(0.5, 0.07, 0.35),
            new SVector(0.42, 0.14, 0.14)
    };

    private static final SVector[] DEFAULT_UI_COLORS_LIGHT = {
            new SVector(1, 1, 1),
            new SVector(0.67, 0.85, 0.95),
            new SVector(0.95, 0.63, 0.84),
            new SVector(0.84, 0.51, 0.51)
    };

    /**
     * https://images.minitool.com/de.minitool.com/images/uploads/news/2022/02/microsoft-paint-herunterladen-installieren/microsoft-paint-herunterladen-installieren-1.png
     */
    public static enum UIColor {
        BACKGROUND_NORMAL(0.24, 0.95),
        BACKGROUND_HIGHLIGHT(0.5, 0.9),
        BACKGROUND_HIGHLIGHT_2(0.7, 0.8),
        OUTLINE_NORMAL(1.1, 0.45),
        OUTLINE_HIGHLIGHT(1.1, 0.45),
        SEPARATOR(0.56, 0.7),
        CANVAS(0.5, 0.8);

        public final double darkModeBrightness, lightModeBrightness;

        private UIColor(double darkModeBrightness, double lightModeBrightness) {
            this.darkModeBrightness = darkModeBrightness;
            this.lightModeBrightness = lightModeBrightness;
        }
    }

    private static boolean darkMode = true;
    private static SVector baseColor = new SVector(darkMode ? DEFAULT_UI_COLORS_DARK[0] : DEFAULT_UI_COLORS_LIGHT[0]);

    private Colors() {
    }

    public static SVector[] getDefaultColors() {
        return darkMode ? DEFAULT_UI_COLORS_DARK : DEFAULT_UI_COLORS_LIGHT;
    }

    public static int getNumDefaultColors() {
        return DEFAULT_UI_COLORS_DARK.length;
    }

    public static boolean isDarkMode() {
        return darkMode;
    }

    public static void setDarkMode(boolean darkMode) {
        Colors.darkMode = darkMode;
    }

    public static void setBaseColor(SVector baseColor) {
        Colors.baseColor.set(baseColor);
    }

    public static SVector getBaseColor() {
        return baseColor;
    }

    public static SVector getBackgroundNormalColor() {
        return getUIColor(UIColor.BACKGROUND_NORMAL);
    }

    public static SVector getBackgroundHighlightColor() {
        return getUIColor(UIColor.BACKGROUND_HIGHLIGHT);
    }

    public static SVector getBackgroundHighlightColor2() {
        return getUIColor(UIColor.BACKGROUND_HIGHLIGHT_2);
    }

    public static SVector getOutlineNormalColor() {
        return getUIColor(UIColor.OUTLINE_NORMAL);
    }

    public static SVector getOutlineHighlightColor() {
        return getUIColor(UIColor.OUTLINE_HIGHLIGHT);
    }

    public static SVector getSeparatorColor() {
        return getUIColor(UIColor.SEPARATOR);
    }

    public static SVector getCanvasColor() {
        return getUIColor(UIColor.CANVAS);
    }

    private static SVector getUIColor(UIColor colorType) {
        double brightness = darkMode ? colorType.darkModeBrightness : colorType.lightModeBrightness;

        brightness = switch (colorType) {
            // case CANVAS -> 0.8;
            // case BACKGROUND_HIGHLIGHT -> 0.83;
            // case BACKGROUND_HIGHLIGHT_2 -> 0.76;
            default -> brightness;
        };

        return new SVector(baseColor).scale(brightness);
    }

    public static SVector getTextColor() {
        return darkMode ? new SVector(1, 1, 1) : new SVector(0, 0, 0);
    }
}