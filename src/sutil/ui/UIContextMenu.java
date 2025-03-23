package sutil.ui;

public class UIContextMenu extends UIFloatMenu {

    private boolean visible;

    public UIContextMenu(UIPanel panel, boolean scroll) {
        super(panel, null, null, scroll);

        // The position and visibility suppliers cannot be put directly into the
        // constructor because of the following error message:
        // Cannot refer to an instance field visible while invoking a constructor
        setPositionGetter(this::getPosition);
        setVisibilitySupplier(this::getVisible);
    }

    public void attachToContainer(UIContainer container) {
        container.setRightClickAction(() -> {
            expandedLabel = null;
            visible = true;
            position.set(container.mousePosition);
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