package sutil.ui;

public record UIMouseButtonAction(int button, boolean mouseAbove, Runnable action) {

    // Either mouse press or mouse release
    public void mouseAction(int button, int mods, boolean isMouseAbove) {
        if ((!mouseAbove || isMouseAbove) && this.button == button)
            action.run();
    }
}