package main.tools;

import org.lwjgl.glfw.GLFW;

import main.apps.MainApp;
import renderEngine.fonts.TextFont;

public class TextTool {

    private static final int DEFAULT_WIDTH = 300,
            DEFAULT_HEIGHT = 50;

    private static final String DEFAULT_FONT_NAME = "Courier New Bold";

    private static final int DEFAULT_SIZE = 28;

    private MainApp app;

    private String text;

    private int x, y, w, h;

    private int size;
    private TextFont font;

    public TextTool(MainApp app) {
        this.app = app;
    }

    public void start() {
        text = "";
        x = y = 0;
        w = DEFAULT_WIDTH;
        h = DEFAULT_HEIGHT;
        size = DEFAULT_SIZE;

        font = app.getLoader().loadFont(DEFAULT_FONT_NAME, (int) size, false);
    }

    public void keyPressed(int key) {
        Character nextChar = null;
        switch (key) {
            case GLFW.GLFW_KEY_BACKSPACE -> {
                if (!text.isEmpty()) {
                    text = text.substring(0, text.length() - 1);
                }
            }
            case GLFW.GLFW_KEY_ENTER -> {
                nextChar = '\n';
            }
            default -> {
                if (Character.isAlphabetic(key)) {
                    nextChar = (char) key;
                }
            }
        }
        if (nextChar != null) {
            text += nextChar;
        }
    }

    public void apply() {
        // Graphics2D graphics = app.getImage().getBufferedImage().createGraphics();
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getW() {
        return w;
    }

    public void setW(int w) {
        this.w = w;
    }

    public int getH() {
        return h;
    }

    public void setH(int h) {
        this.h = h;
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