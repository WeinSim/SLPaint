package sutil.ui;

import org.lwjglx.util.vector.Vector4f;

public enum UIColors {

    BACKGROUND_NORMAL(0.24, 0.95),
    BACKGROUND_HIGHLIGHT(0.5, 0.9),
    BACKGROUND_HIGHLIGHT_2(0.7, 0.85),
    OUTLINE_NORMAL(1.1, 0.45),
    OUTLINE_HIGHLIGHT(0.9, 0.6),
    SEPARATOR(0.56, 0.7),
    CANVAS(0.4, 0.85),
    TRANSPARENCY_1(0.6, 1),
    TRANSPARENCY_2(0.0, 0.85),
    TEXT(new Vector4f(1f, 1f, 1f, 1f), new Vector4f(0f, 0f, 0f, 1f)),
    SELECTION_BORDER_1(new Vector4f(0f, 0f, 0f, 1f), new Vector4f(0f, 0f, 0f, 1f)),
    SELECTION_BORDER_2(new Vector4f(1f, 1f, 1f, 1f), new Vector4f(1f, 1f, 1f, 1f));

    public final double darkModeBrightness, lightModeBrightness;

    public final Vector4f darkColor, lightColor;

    public final boolean useBrightness;

    private UIColors(double darkModeBrightness, double lightModeBrightness) {
        this.darkModeBrightness = darkModeBrightness;
        this.lightModeBrightness = lightModeBrightness;

        useBrightness = true;

        darkColor = null;
        lightColor = null;
    }

    private UIColors(Vector4f darkColor, Vector4f lightColor) {
        this.darkColor = darkColor;
        this.lightColor = lightColor;

        useBrightness = false;

        darkModeBrightness = 0;
        lightModeBrightness = 0;
    }
}