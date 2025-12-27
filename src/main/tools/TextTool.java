package main.tools;

import org.lwjgl.glfw.GLFW;

import sutil.math.SVector;

public final class TextTool extends ImageTool implements XYWH {

    public static final TextTool INSTANCE;

    public static final String[] FONT_NAMES;
    private static final String DEFAULT_FONT_NAME;

    private static final int MIN_TEXT_SIZE = 0,
            MAX_TEXT_SIZE = 128,
            DEFAULT_TEXT_SIZE = 32;

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

    // INITIAL_DRAG
    private int startX, startY;
    private int endX, endY;
    // IDLE, IDLE_DRAG
    private int x, y;
    private int width, height;

    private TextTool() {
        size = DEFAULT_TEXT_SIZE;
        text = "";

        addKeyboardShortcut(new KeyboardShortcut(GLFW.GLFW_KEY_CAPS_LOCK, 0, IDLE, NONE, this::flattenText));
    }

    @Override
    public boolean startInitialDrag(int x, int y, int mouseButton) {
        if (mouseButton != GLFW.GLFW_MOUSE_BUTTON_LEFT)
            return false;

        startX = Math.min(Math.max(0, x), app.getImage().getWidth());
        startY = Math.min(Math.max(0, y), app.getImage().getHeight());

        text = "";

        return true;
    }

    @Override
    protected void handleInitialDrag(int x, int y, int px, int py) {
        endX = Math.min(Math.max(0, x), app.getImage().getWidth());
        endY = Math.min(Math.max(0, y), app.getImage().getHeight());
    }

    @Override
    protected boolean finishInitialDrag() {
        x = Math.min(startX, endX);
        y = Math.min(startY, endY);
        width = Math.abs(startX - endX);
        height = Math.abs(startY - endY);

        // if (width == 0 || height == 0)
        // return false;

        app.getUI().select(app.getTextToolInput());

        return true;
    }

    @Override
    protected boolean startIdleDrag(int x, int y, int mouseButton) {
        flattenText();
        return false;
    }

    @Override
    protected void handleIdleDrag(int x, int y, int px, int py) {
        invalidState();
    }

    @Override
    protected void finishIdleDrag() {
        invalidState();
    }

    @Override
    public void forceQuit() {
        if (!text.isEmpty()) {
            flattenText();
        }

        super.forceQuit();
    }

    private void invalidState() {
        final String baseString = "INITIAL_DRAG, IDLE and IDLE_DRAG states undefined for %s tool";
        throw new UnsupportedOperationException(String.format(baseString, getName()));
    }

    @Override
    public String getName() {
        return "Text";
    }

    private void flattenText() {
        SVector position = app.getImagePosition(app.getTextToolInput().getAbsolutePosition());
        app.renderTextToImage(text, position.x, position.y, size, app.getLoader().loadFont(font));
        text = "";
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return switch (getState()) {
            case INITIAL_DRAG -> Math.abs(startX - endX);
            default -> width;
        };
    }

    public int getHeight() {
        return switch (getState()) {
            case INITIAL_DRAG -> Math.abs(startY - endY);
            default -> height;
        };
    }

    public int getStartX() {
        return startX;
    }

    public int getStartY() {
        return startY;
    }

    public int getEndX() {
        return endX;
    }

    public int getEndY() {
        return endY;
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