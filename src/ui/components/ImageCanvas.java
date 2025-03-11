package ui.components;

import main.apps.MainApp;
import sutil.ui.UIContainer;
import sutil.ui.UIElement;

public class ImageCanvas extends UIContainer {

    public ImageCanvas(int orientation, int alignment, MainApp app) {
        super(orientation, alignment);

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