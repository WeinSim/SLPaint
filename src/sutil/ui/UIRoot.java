package sutil.ui;

import sutil.math.SVector;

public class UIRoot extends UIContainer {

    public UIRoot(UIPanel panel, int orientation, int alignment) {
        super(orientation, alignment);
        this.panel = panel;
        panel.setRoot(this);

        backgroundNormal = true;
        outlineNormal = false;
    }

    @Override
    public SVector getAbsolutePosition() {
        return new SVector();
    }
}