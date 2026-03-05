package sutil.ui.elements;

import java.util.function.Supplier;

import sutil.math.SVector;
import sutil.ui.UI;
import sutil.ui.UISizes;

public class UIIcon extends UIImage {

    public UIIcon(String name) {
        this(name, UISizes.ICON::getWidthHeight);
    }

    public UIIcon(String name, Supplier<SVector> sizeSupplier) {
        super(UI.getIconTextureID(name), sizeSupplier);
    }
}