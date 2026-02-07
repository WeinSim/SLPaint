package sutil.ui.elements;

import java.util.function.Supplier;

public class UIMenuButton extends UIContainer {

    private boolean expanded;

    private final UIFloatMenu dropdown;

    public UIMenuButton(String text) {
        this(() -> text, false);
    }

    public UIMenuButton(String text, boolean scroll) {
        this(() -> text, scroll);
    }

    public UIMenuButton(Supplier<String> textUpdater) {
        this(textUpdater, false);
    }

    public UIMenuButton(Supplier<String> textUpdater, boolean scroll) {
        super(CENTER, CENTER);

        noBackground();
        withOutline();
        zeroPadding();
        backgroundHighlight = true;

        add(new UIText(textUpdater));

        setLeftClickAction(() -> expanded = !expanded);

        dropdown = new UIFloatMenu(() -> expanded, () -> expanded = false, scroll, UIText.NORMAL);
        // see comment in constructor of MenuLabel (UIMenuBar.java:36)
        dropdown.setRelativeLayer(4);
        dropdown.addAnchor(UIFloatContainer.Anchor.TOP_LEFT, this, UIFloatContainer.Anchor.BOTTOM_LEFT);
        dropdown.addAnchor(UIFloatContainer.Anchor.BOTTOM_LEFT, this, UIFloatContainer.Anchor.TOP_LEFT);

        add(dropdown);

        expanded = false;
    }

    public void addLabel(String text, Runnable clickAction) {
        dropdown.addLabel(text, clickAction);
    }
}