package sutil.ui;

import sutil.math.SVector;

public class UIEmpty extends UIElement {

    public UIEmpty(double wh) {
        size = new SVector(wh, wh);
    }

    public UIEmpty(double width, double height) {
        size = new SVector(width, height);
    }

    public UIEmpty(SVector size) {
        this.size = size;
    }

    @Override
    public void setPreferredSize() {
    }
}