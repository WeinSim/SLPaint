package ui.components;

import sutil.ui.UIContainer;
import sutil.ui.UIElement;
import sutil.ui.UISeparator;

public class SeparatorContainer extends UIContainer {

    public SeparatorContainer(int orientation, int alignment) {
        super(orientation, alignment);
    }

    @Override
    public void add(UIElement child) {
        if (children.size() > 0) {
            super.add(new UISeparator());
        }
        super.add(child);
    }

    @Override
    public double getMargin() {
        return 2 * super.getMargin();
    }
}