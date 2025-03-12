package sutil.ui;

import java.util.function.Supplier;

import sutil.math.SVector;

public class UIFloatContainer extends UIContainer {

    private Supplier<SVector> positionGetter;

    public UIFloatContainer(int orientation, int alignment, SVector position) {
        this(orientation, alignment, (Supplier<SVector>) null);
        this.position.set(position);
    }

    public UIFloatContainer(int orientation, int alignment, Supplier<SVector> positionGetter) {
        super(orientation, alignment);
        this.positionGetter = positionGetter;
    }

    @Override
    public void positionChildren() {
        if (positionGetter != null) {
            position.set(positionGetter.get());
        }
        super.positionChildren();
    }
}