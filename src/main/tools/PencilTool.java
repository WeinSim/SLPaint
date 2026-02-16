package main.tools;

import org.lwjgl.glfw.GLFW;

import main.image.Image;

public final class PencilTool extends ImageTool {

    public static final PencilTool INSTANCE = new PencilTool();

    public static final int MIN_SIZE = 1, MAX_SIZE = 16;

    public static final int DRAWING_PRIMARY = 0x02, DRAWING_SECONDARY = 0x04;

    private int size = 1;

    private PencilTool() {
        super();
    }

    @Override
    public void createKeyboardShortcuts() {
    }

    @Override
    public void click(int x, int y, int mouseButton) {
        if (mouseButton != GLFW.GLFW_MOUSE_BUTTON_LEFT && mouseButton != GLFW.GLFW_MOUSE_BUTTON_RIGHT)
            return;

        Image image = app.getImage();

        if (!image.isInside(x, y))
            return;

        if (x >= 0 && x < image.getWidth() && y >= 0 && y < image.getHeight()) {
            state = switch (mouseButton) {
                case GLFW.GLFW_MOUSE_BUTTON_LEFT -> DRAWING_PRIMARY;
                case GLFW.GLFW_MOUSE_BUTTON_RIGHT -> DRAWING_SECONDARY;
                // can never happen
                default -> 0;
            };
        }
    }
    
    @Override
    public void finish() {
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = Math.min(Math.max(size, MIN_SIZE), MAX_SIZE);
    }

    @Override
    public String getName() {
        return "Pencil";
    }
}