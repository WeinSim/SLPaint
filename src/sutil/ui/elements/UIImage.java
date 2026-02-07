package sutil.ui.elements;

import java.util.function.IntSupplier;

import sutil.math.SVector;

public class UIImage extends UIElement {

    protected int textureID;
    protected IntSupplier textureIDSupplier;

    public UIImage(int textureID, SVector size) {
        setTextureID(textureID);

        setSize(size);
    }

    public UIImage(IntSupplier textureIDSupplier, SVector size) {
        this.textureIDSupplier = textureIDSupplier;

        setSize(size);
    }

    public void setSize(SVector size) {
        this.size.set(size);
    }

    @Override
    public void setPreferredSize() {
    }

    public int getTextureID() {
        return textureIDSupplier != null ? textureIDSupplier.getAsInt() : textureID;
    }

    public void setTextureID(int textureID) {
        this.textureID = textureID;
        textureIDSupplier = null;
    }

    public void setTextureIDSupplier(IntSupplier textureIDSupplier) {
        this.textureIDSupplier = textureIDSupplier;
    }
}