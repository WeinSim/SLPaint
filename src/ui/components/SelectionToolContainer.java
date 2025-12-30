package ui.components;

import org.lwjgl.glfw.GLFW;

import main.apps.MainApp;
import main.tools.ImageTool;
import main.tools.SelectionTool;

public class SelectionToolContainer extends ToolContainer<SelectionTool> {

    public SelectionToolContainer(MainApp app) {
        super(ImageTool.SELECTION, app);

        // Ctrl + A: select everything
        addKeyboardShortcut(new KeyboardShortcut(
                GLFW.GLFW_KEY_A, GLFW.GLFW_MOD_CONTROL, NONE | IDLE, IDLE, tool::selectEverything));

        // Esc: finish selection
        addKeyboardShortcut(new KeyboardShortcut(
                GLFW.GLFW_KEY_CAPS_LOCK, 0, IDLE, NONE, tool::finish));

        // Del: delete selection
        addKeyboardShortcut(new KeyboardShortcut(
                GLFW.GLFW_KEY_DELETE, 0, IDLE, NONE, tool::clearSelection));

        // Ctrl + V: paste
        addKeyboardShortcut(new KeyboardShortcut(
                GLFW.GLFW_KEY_V, GLFW.GLFW_MOD_CONTROL, NONE | IDLE, IDLE, tool::pasteFromClipboard));

        // Ctrl + C: copy
        addKeyboardShortcut(new KeyboardShortcut(
                GLFW.GLFW_KEY_C, GLFW.GLFW_MOD_CONTROL, IDLE, IDLE, tool::copyToClipboard));

        // Ctrl + X: cut
        addKeyboardShortcut(new KeyboardShortcut(
                GLFW.GLFW_KEY_X, GLFW.GLFW_MOD_CONTROL, IDLE, NONE, tool::cutToClipboard));


        // Arrow keys: move selection
        addKeyboardShortcut(new KeyboardShortcut(GLFW.GLFW_KEY_UP, 0, IDLE, IDLE, () -> tool.moveSelection(0, -1)));
        addKeyboardShortcut(new KeyboardShortcut(GLFW.GLFW_KEY_DOWN, 0, IDLE, IDLE, () -> tool.moveSelection(0, 1)));
        addKeyboardShortcut(new KeyboardShortcut(GLFW.GLFW_KEY_LEFT, 0, IDLE, IDLE, () -> tool.moveSelection(-1, 0)));
        addKeyboardShortcut(new KeyboardShortcut(GLFW.GLFW_KEY_RIGHT, 0, IDLE, IDLE, () -> tool.moveSelection(1, 0)));
    }
}