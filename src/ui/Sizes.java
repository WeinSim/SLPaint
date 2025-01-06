package ui;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

public class Sizes {

    /**
     * Values for a 1920x1080 screen
     */
    private static final double TEXT_SIZE = 18,
        MARGIN = 10,
        PADDING = 10,
        COLOR_BUTTON_SIZE = 28,
        BIG_COLOR_BUTTON_SIZE = 36,
        COLOR_PICKER_SIZE_SIDE_PANEL = 200,
        COLOR_PICKER_SIZE_EXTRA_WINDOW = 300,
        COLOR_PICKER_PREVIEW_WIDTH = 120,
        COLOR_PICKER_PREVIEW_HEIGHT = 80;

    private static double uiScale;

    static {
        // This returns the virtual screenSize (1440 on my surface, which is exactly
        // half the actual resolution because the system's uiScale factor has already
        // been applied)
        // Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        // System.out.format("Toolkit: width = %d\n", (int) screenSize.getWidth());

        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        int width = gd.getDisplayMode().getWidth();

        uiScale = width / 1920.0;
    }

    private Sizes() {

    }

    public static double getTextSize() {
        return TEXT_SIZE * uiScale;
    }

    public static double getMargin() {
        return MARGIN * uiScale;
    }

    public static double getPadding() {
        return PADDING * uiScale;
    }

    public static double getColorButtonSize() {
        return COLOR_BUTTON_SIZE * uiScale;
    }

    public static double getBigColorButtonSize() {
        return BIG_COLOR_BUTTON_SIZE * uiScale;
    }

    public static double getColorPickerSizeSidePanel() {
        return COLOR_PICKER_SIZE_SIDE_PANEL * uiScale;
    }

    public static double getColorPickerSizeExtraWindow() {
        return COLOR_PICKER_SIZE_EXTRA_WINDOW * uiScale;
    }

    public static double getColorPickerPreviewWidth() {
        return COLOR_PICKER_PREVIEW_WIDTH * uiScale;
    }

    public static double getColorPickerPreviewHeight() {
        return COLOR_PICKER_PREVIEW_HEIGHT * uiScale;
    }
}