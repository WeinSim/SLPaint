package ui.components;

import main.apps.MainApp;
import sutil.ui.UIContainer;
import sutil.ui.UIElement;

public class ImageCanvas extends UIContainer {

    public ImageCanvas(int orientation, int alignment, MainApp app) {
        this(orientation, alignment, alignment, app);
    }

    public ImageCanvas(int orientation, int hAlignment, int vAlignment, MainApp app) {
        super(orientation, hAlignment, vAlignment);

        app.setCanvas(this);
        setMaximalSize();
    }

    public boolean mouseTrulyAbove() {
        if (!mouseAbove) {
            return false;
        }

        for (UIElement child : getChildren()) {
            if (child.mouseAbove()) {
                return false;
            }
        }

        return true;
    }
}