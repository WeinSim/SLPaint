package main.tools;

import sutil.math.SVector;

public final class TextTool extends DragTool {

    public static final TextTool INSTANCE;

    public static final String[] FONT_NAMES;
    private static final String DEFAULT_FONT_NAME;

    private static final int MIN_TEXT_SIZE = 0,
            MAX_TEXT_SIZE = 128,
            DEFAULT_TEXT_SIZE = 32;

    public static final int MARGIN = 5;

    static {
        // this method takes ~300 milliseconds to run!
        // long startTime = System.nanoTime();
        // FONT_NAMES =
        // GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        // Font[] fonts =
        // GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
        // FONT_NAMES = new String[fonts.length];
        // for (int i = 0; i < fonts.length; i++) {
        // FONT_NAMES[i] = fonts[i].getName();
        // }
        // System.out.format("getFontFamilyNames: %.3fms\n", (System.nanoTime() -
        // startTime) * 1e-6);

        FONT_NAMES = new String[] { "Courier New Bold" };

        DEFAULT_FONT_NAME = "Courier New Bold";
        // DEFAULT_FONT_NAME = (new Font("Courier", Font.PLAIN, 1)).getFamily();

        INSTANCE = new TextTool();
    }

    private String text;
    private int size;
    private String font = DEFAULT_FONT_NAME;

    private TextTool() {
        size = DEFAULT_TEXT_SIZE;
        // text = "";

        // addKeyboardShortcut(new KeyboardShortcut(GLFW.GLFW_KEY_CAPS_LOCK, 0, IDLE,
        // NONE, this::flattenText));
    }

    // @Override
    // public boolean startInitialDrag(int x, int y, int mouseButton) {
    // if (super.startInitialDrag(x, y, mouseButton)) {
    // text = "";
    // return true;
    // }
    // return false;
    // }

    @Override
    public int getMargin() {
        return MARGIN;
    }

    @Override
    public boolean enterIdle() {
        return true;
    }

    @Override
    public void init() {
        app.getUI().select(app.getTextToolInput());
        text = "";
    }

    @Override
    public void finish() {
        SVector position = app.getImagePosition(app.getTextToolInput().getAbsolutePosition());
        app.renderTextToImage(text, position.x, position.y, size, app.getLoader().loadFont(font));
    }

    // private void invalidState() {
    // final String baseString = "INITIAL_DRAG, IDLE and IDLE_DRAG states undefined
    // for %s tool";
    // throw new UnsupportedOperationException(String.format(baseString,
    // getName()));
    // }

    @Override
    public String getName() {
        return "Text";
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = Math.min(Math.max(MIN_TEXT_SIZE, size), MAX_TEXT_SIZE);
    }

    public String getFont() {
        return font;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}