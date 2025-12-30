package ui;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

public enum Sizes {

    MAIN_APP(1280, 720),
    SETTINGS_APP(900, 650),
    TEXT(18),
    MARGIN(10),
    PADDING(10),
    STROKE_WEIGHT(1.5, true),
    COLOR_BUTTON(30),
    BIG_COLOR_BUTTON(45),
    CHECKERBOARD_SIZE(15),
    UI_SCALE_MARGIN(13),
    COLOR_PICKER_SIDE_PANEL(200),
    COLOR_PICKER_EXTRA_WINDOW(300),
    COLOR_PICKER_PREVIEW(120, 75);

    public final double size;
    public final double width;
    public final double height;

    private Sizes(double size) {
        this(size, false);
    }

    private Sizes(double size, boolean forceInteger) {
        this(size, size, size, forceInteger);
    }

    private Sizes(double width, double height) {
        this(width, height, false);
    }

    private Sizes(double width, double height, boolean forceInteger) {
        this(0, width, height, false);
    }

    private Sizes(double size, double width, double height, boolean forceInteger) {
        size = size * Inner.UI_SCALE;
        width = width * Inner.UI_SCALE;
        height = height * Inner.UI_SCALE;
        if (forceInteger) {
            size = (int) size;
            width = (int) width;
            height = (int) height;
        }
        this.size = size;
        this.width = width;
        this.height = height;
    }

    public static double getUIScale() {
        return Inner.UI_SCALE;
    }

    /**
     * The reason why this class exists (instead of uiScale being a static variable
     * of Sizes directly) is that enum values are initiated before static
     * variables, and so its value cannot be used in the Sizes() constructor.
     */
    private static class Inner {

        private static final double UI_SCALE;

        static {
            // This returns the virtual screenSize (width=1440 on my surface, which is
            // exactly
            // half the actual resolution because the system's uiScale factor has already
            // been applied)
            // Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            // System.out.format("Toolkit: width = %d\n", (int) screenSize.getWidth());

            GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            int width = gd.getDisplayMode().getWidth();

            UI_SCALE = width / 1920.0;
        }
    }
}