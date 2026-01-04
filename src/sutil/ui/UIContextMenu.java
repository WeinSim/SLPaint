package sutil.ui;

import sutil.math.SVector;

public class UIContextMenu extends UIFloatMenu {

    private boolean visible;

    public UIContextMenu(boolean scroll) {
        super(scroll);

        // The position and visibility suppliers cannot be put directly into the
        // constructor because of the following error message:
        // Cannot refer to an instance field visible while invoking a constructor
        setVisibilitySupplier(this::getVisible);
    }

    public void attachToContainer(UIContainer container) {
        container.setRightClickAction(() -> {
            expandedLabel = null;
            visible = true;
            clearAttachPoints();
            SVector basePos = new SVector(container.mousePosition);
            addAnchor(Anchor.TOP_LEFT, basePos);
            addAnchor(Anchor.TOP_RIGHT, basePos);
            addAnchor(Anchor.BOTTOM_LEFT, basePos);
            addAnchor(Anchor.BOTTOM_RIGHT, basePos);
        });
        container.setLeftClickAction(this::hide);
        container.add(this);
    }

    private boolean getVisible() {
        return visible;
    }

    private void hide() {
        visible = false;
    }
}