package sutil.ui.elements;

import sutil.ui.UI;
import sutil.ui.UIColors;
import sutil.ui.elements.UIFloatContainer.Anchor;

public class UIMenuBar extends UIContainer {

    private MenuLabel expandedMenu;

    public UIMenuBar() {
        super(UI.HORIZONTAL, UI.LEFT, UI.CENTER);

        noOutline();
        setMarginScale(0.5);
        setPaddingScale(0.5);
        setHFillSize();

        expandedMenu = null;
    }

    public UIFloatMenu addMenu(String name) {
        MenuLabel menuLabel = new MenuLabel(name);
        add(menuLabel);
        return menuLabel.menu;
    }

    private class MenuLabel extends UIContainer {

        final UIFloatMenu menu;

        MenuLabel(String text) {
            super(UI.VERTICAL, UI.CENTER);

            this.menu = new UIFloatMenu(() -> expandedMenu == this, () -> expandedMenu = null);
            // TODO: this is just a hack to prevent parts of the image canvas (e.g. size
            // knobs) to appear above the menu.
            menu.setRelativeLayer(4);
            menu.addAnchor(Anchor.TOP_LEFT, this, Anchor.BOTTOM_LEFT);

            noOutline();

            style.setBackgroundColor(
                    () -> (mouseAbove || expandedMenu == this)
                            ? UIColors.BACKGROUND_HIGHLIGHT.get()
                            : null);

            setLeftClickAction(() -> {
                if (expandedMenu == null) {
                    expandedMenu = this;
                } else {
                    expandedMenu = null;
                }
            });

            add(new UIText(text, UIText.SMALL));
            add(menu);
        }

        @Override
        public void update() {
            super.update();

            if (mouseAbove && expandedMenu != null) {
                expandedMenu = this;
            }
        }

        @Override
        public void mousePressed(int mouseButton, int mods) {
            super.mousePressed(mouseButton, mods);

            if (expandedMenu == this) {
                if (!mouseAbove() && !expandedMenu.mouseAbove()) {
                    expandedMenu = null;
                }
            }
        }
    }
}