package sutil.ui;

import sutil.ui.UIFloatContainer.Anchor;

public class UIMenuBar extends UIContainer {

    private UIFloatMenu expandedMenu;

    public UIMenuBar() {
        super(UI.HORIZONTAL, UI.LEFT, UI.CENTER);

        noOutline();
        setMarginScale(0.5);
        setPaddingScale(0.5);

        expandedMenu = null;
    }

    public void addMenu(String name, UIFloatMenu menu) {
        add(new MenuLabel(name, menu));
    }

    public boolean isExpanded() {
        return expandedMenu != null;
    }

    public void setExpandedMenu(UIFloatMenu expandedMenu) {
        this.expandedMenu = expandedMenu;
    }

    private class MenuLabel extends UIContainer {

        final UIFloatMenu menu;

        MenuLabel(String text, UIFloatMenu menu) {
            this.menu = menu;

            super(UI.VERTICAL, UI.CENTER);

            noOutline();

            style.setBackgroundColor(
                    () -> (mouseAbove || expandedMenu == menu)
                            ? UIColors.BACKGROUND_HIGHLIGHT.get()
                            : null);

            setLeftClickAction(() -> {
                if (expandedMenu == null) {
                    expandedMenu = menu;
                } else {
                    expandedMenu = null;
                }
            });

            add(new UIText(text, UIText.SMALL));

            // TODO: this is just a hack to prevent parts of the image canvas (e.g. size
            // knobs) to appear above the menu.
            menu.setRelativeLayer(4);
            menu.setVisibilitySupplier(() -> expandedMenu == menu);
            menu.setCloseAction(() -> expandedMenu = null);
            menu.addAnchor(Anchor.TOP_LEFT, this, Anchor.BOTTOM_LEFT);
            add(menu);
        }

        @Override
        public void update() {
            super.update();

            if (mouseAbove && expandedMenu != null) {
                expandedMenu = menu;
            }
        }

        @Override
        public void mousePressed(int mouseButton, int mods) {
            super.mousePressed(mouseButton, mods);

            if (expandedMenu == menu) {
                if (!mouseAbove() && !expandedMenu.mouseAbove()) {
                    expandedMenu = null;
                }
            }
        }
    }
}