package ui.components;

import main.apps.MainApp;
import sutil.ui.UIContainer;

public class ImageCanvas extends UIContainer {

    public ImageCanvas(int orientation, int alignment, MainApp app) {
        super(orientation, alignment);

        app.setCanvas(this);
        setMaximalSize();
    }
}