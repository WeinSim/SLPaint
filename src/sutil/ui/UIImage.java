package sutil.ui;

import java.util.function.Supplier;

import sutil.math.SVector;

public class UIImage extends UIElement {

    protected int textureID;
    protected Supplier<Integer> textureIDSupplier;

    public UIImage(int textureID, SVector size) {
        setTextureID(textureID);

        setSize(size);
    }

    public UIImage(Supplier<Integer> textureIDSupplier, SVector size) {
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
        return textureIDSupplier != null ? textureIDSupplier.get() : textureID;
    }

    public void setTextureID(int textureID) {
        this.textureID = textureID;
        textureIDSupplier = null;
    }

    public void setTextureIDSupplier(Supplier<Integer> textureIDSupplier) {
        this.textureIDSupplier = textureIDSupplier;
    }
}