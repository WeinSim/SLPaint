package main.tools;

import org.lwjgl.glfw.GLFW;

import renderEngine.fonts.TextFont;

public final class TextTool extends ImageTool implements XYWH {

    public static final TextTool INSTANCE = new TextTool();

    private static final String DEFAULT_FONT_NAME = "Courier New Bold";

    private static final int DEFAULT_SIZE = 64;

    private String text;

    // INITIAL_DRAG
    private int startX, startY;
    private int endX, endY;
    // IDLE, IDLE_DRAG
    private int x, y;
    private int width, height;

    // private String text;
    private int size;
    private TextFont font;

    private TextTool() {
        size = DEFAULT_SIZE;

        addKeyboardShortcut(new KeyboardShortcut(GLFW.GLFW_KEY_CAPS_LOCK, 0, IDLE, NONE, this::flattenText));
    }

    @Override
    public void start() {
        super.start();

        if (font == null) {
            font = app.getLoader().loadFont(DEFAULT_FONT_NAME, size, false);
        }
        text = "";
    }

    @Override
    public boolean startInitialDrag(int x, int y, int mouseButton) {
        if (mouseButton != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return false;
        }

        start();

        startX = Math.min(Math.max(0, x), app.getImage().getWidth());
        startY = Math.min(Math.max(0, y), app.getImage().getHeight());

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

        if (width == 0 || height == 0) {
            return false;
        }

        app.getUI().setSelectedElement(app.getTextToolInput());

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
        flattenText();
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
        app.renderTextToImage(text, x, y, size, font);
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
        this.size = size;
    }

    public TextFont getFont() {
        return font;
    }

    public void setFont(TextFont font) {
        this.font = font;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}