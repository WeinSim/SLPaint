package sutil.ui;

import sutil.math.SVector;

public class UIContextMenu extends UIFloatMenu {

    private boolean visible;

    public UIContextMenu(UIPanel panel, boolean scroll) {
        super(panel, null, scroll);

        // The position and visibility suppliers cannot be put directly into the
        // constructor because of the following error message:
        // Cannot refer to an instance field visible while invoking a constructor
        setVisibilitySupplier(this::getVisible);
        // setPositionSupplier(this::getPosition);
    }

    public void attachToContainer(UIContainer container) {
        container.setRightClickAction(() -> {
            expandedLabel = null;
            visible = true;
            clearAttachPoints();
            SVector basePos = new SVector(container.mousePosition);
            addAttachPoint(TOP_LEFT, basePos);
            addAttachPoint(TOP_RIGHT, basePos);
            addAttachPoint(BOTTOM_LEFT, basePos);
            addAttachPoint(BOTTOM_RIGHT, basePos);
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