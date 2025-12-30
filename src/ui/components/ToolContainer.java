package ui.components;

import java.util.ArrayList;

import org.lwjgl.glfw.GLFW;

import main.apps.MainApp;
import main.tools.DragTool;
import sutil.math.SVector;
import sutil.ui.UIContainer;

public abstract class ToolContainer<T extends DragTool> extends UIContainer {

    public static final int NONE = 0x01, INITIAL_DRAG = 0x02, IDLE = 0x04, IDLE_DRAG = 0x08;

    protected int state;

    // INITIAL_DRAG
    private int startX, startY;
    private int endX, endY;
    // IDLE_DRAG
    private SVector dragStartMouseCoords;
    private int dragStartX, dragStartY;

    private ArrayList<KeyboardShortcut> keyboardShortcuts;

    protected final MainApp app;
    protected final T tool;

    public ToolContainer(T tool, MainApp app) {
        super(0, 0);

        this.tool = tool;
        this.app = app;

        zeroMargin();
        noBackground();
        // TODO: checkerboard outline
        withOutline();
        style.setStrokeWeightSupplier(() -> state == NONE ? 0.0 : 2.0);

        setVisibilitySupplier(() -> app.getActiveTool() == tool);
        setLeftClickAction(this::startIdleDrag);

        relativeLayer = 1;

        state = NONE;
        keyboardShortcuts = new ArrayList<>();
    }

    @Override
    public void updateVisibility() {
        boolean visibleBefore = isVisible();

        super.updateVisibility();

        if (visibleBefore && !isVisible()) {
            // a different tool was selected
            tool.finish();
        }
    }

    @Override
    public void update() {
        switch (state) {
            case INITIAL_DRAG -> {
                int[] mouseImagePos = app.getMouseImagePosition();
                int mouseX = mouseImagePos[0],
                        mouseY = mouseImagePos[1];

                endX = Math.min(Math.max(0, mouseX), app.getImage().getWidth() - 1);
                endY = Math.min(Math.max(0, mouseY), app.getImage().getHeight() - 1);

                int margin = tool.getMargin();

                int x = Math.min(startX, endX) + margin;
                int y = Math.min(startY, endY) + margin;
                int width = Math.abs(startX - endX) + 1 - 2 * margin;
                int height = Math.abs(startY - endY) + 1 - 2 * margin;

                tool.setX(x);
                tool.setY(y);
                tool.setWidth(width);
                tool.setHeight(height);
            }
            case IDLE_DRAG -> {
                SVector delta = app.getMouseImagePosVec().copy().sub(dragStartMouseCoords);
                int x = dragStartX + (int) Math.round(delta.x);
                int y = dragStartY + (int) Math.round(delta.y);

                tool.setX(x);
                tool.setY(y);
            }
        }

        setFixedSize(new SVector(tool.getWidth(), tool.getHeight()).scale(app.getImageZoom()));
    }

    public void keyPressed(int key, int modifiers) {
        for (KeyboardShortcut shortcut : keyboardShortcuts) {
            if (shortcut.key() == key
                    && (shortcut.initialState() & state) != 0
                    && shortcut.modifiers() == modifiers) {
                // final String baseString = "%s: executing shortcut. key = %d, modifiers = %d,
                // initialState = %d, finalState = %d\n";
                // System.out.format(baseString, getName(), shortcut.key(),
                // shortcut.modifiers(), shortcut.initialState(),
                // shortcut.finalState());
                app.setActiveTool(tool);
                shortcut.action().run();
                state = shortcut.finalState;
                break;
            }
        }
    }

    @Override
    public void mouseReleased(int mouseButton, int mods) {
        switch (mouseButton) {
            case GLFW.GLFW_MOUSE_BUTTON_LEFT -> {
            }
            case GLFW.GLFW_MOUSE_BUTTON_RIGHT -> {
                switch (state) {
                    case INITIAL_DRAG -> {
                        if (tool.enterIdle()) {
                            tool.init();
                            state = IDLE;
                        }
                    }
                    case IDLE_DRAG -> {
                        state = IDLE;
                    }
                }
            }
        }
    }

    public void startInitialDrag() {
        int[] mouseImagePos = app.getMouseImagePosition();
        int x = mouseImagePos[0],
                y = mouseImagePos[1];
        startX = Math.min(Math.max(0, x), app.getImage().getWidth() - 1);
        startY = Math.min(Math.max(0, y), app.getImage().getHeight() - 1);

        state = INITIAL_DRAG;
    }

    protected void startIdleDrag() {
        dragStartMouseCoords = app.getMouseImagePosVec();
        dragStartX = tool.getX();
        dragStartY = tool.getY();
    }

    protected boolean showChildren() {
        return state == IDLE || state == IDLE_DRAG;
    }

    public T getTool() {
        return tool;
    }

    protected void addKeyboardShortcut(KeyboardShortcut shortcut) {
        keyboardShortcuts.add(shortcut);
    }

    protected record KeyboardShortcut(int key, int modifiers, int initialState, int finalState, Runnable action) {
    }
}