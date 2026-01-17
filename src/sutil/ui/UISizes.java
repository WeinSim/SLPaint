package sutil.ui;

public enum UISizes {

    MAIN_APP(1280, 720),
    SETTINGS_APP(900, 650),
    TEXT(18),
    TEXT_SMALL(14),
    MARGIN(10),
    PADDING(10),
    STROKE_WEIGHT(1.2, true),
    SCROLLBAR(15),
    SCALE_NARROW(2),
    SCALE_WIDE(20),
    SCALE_SLIDER_LENGTH(10),
    SCALE_SLIDER_WIDTH(2),
    COLOR_BUTTON(24),
    BIG_COLOR_BUTTON(36),
    CHECKERBOARD(12),
    COLOR_PICKER_PANEL(200),
    COLOR_PICKER_EXTRA_WINDOW(300),
    COLOR_PICKER_PREVIEW(120, 75),
    SIZE_KNOB(15);

    public final double size;
    public final double width;
    public final double height;
    public final boolean forceInteger;

    private UISizes(double size) {
        this(size, false);
    }

    private UISizes(double size, boolean forceInteger) {
        this(size, size, size, forceInteger);
    }

    private UISizes(double width, double height) {
        this(width, height, false);
    }

    private UISizes(double width, double height, boolean forceInteger) {
        this(0, width, height, false);
    }

    private UISizes(double size, double width, double height, boolean forceInteger) {
        this.size = size;
        this.width = width;
        this.height = height;
        this.forceInteger = forceInteger;
    }
}