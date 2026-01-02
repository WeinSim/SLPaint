package sutil.ui;

import sutil.math.SVector;

public class UIImage extends UIElement {

    protected int textureID;

    public UIImage(int textureID, SVector size) {
        super();
        this.textureID = textureID;

        setSize(size);
    }

    public void setSize(SVector size) {
        this.size.set(size);
    }

    @Override
    public void setPreferredSize() {
    }

    public int getTextureID() {
        return textureID;
    }

    public void setTextureID(int textureID) {
        this.textureID = textureID;
    }
}