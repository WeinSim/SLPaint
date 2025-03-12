package ui.components;

import sutil.ui.UIContainer;
import sutil.ui.UIElement;
import sutil.ui.UISeparator;

public class SeparatorContainer extends UIContainer {

    private boolean addInitialSeparator;

    public SeparatorContainer(int orientation, int alignment) {
        this(orientation, alignment, false);
    }

    public SeparatorContainer(int orientation, int alignment, boolean addInitialSeparator) {
        super(orientation, alignment);

        this.addInitialSeparator = addInitialSeparator;
    }

    @Override
    public void add(UIElement child) {
        if (addInitialSeparator || getChildren().size() > 0) {
            UISeparator separator = new UISeparator();
            separator.setVisibilitySupplier(() -> child.isVisible());
            super.add(separator);
        }
        super.add(child);
    }

    @Override
    public double getMargin() {
        return 2 * super.getMargin();
    }
}