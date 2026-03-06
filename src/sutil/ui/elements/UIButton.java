package sutil.ui.elements;

import java.util.function.BooleanSupplier;

import sutil.ui.UIIcon;

public class UIButton extends UIContainer {

    protected static final BooleanSupplier TRUE = () -> true;

    protected BooleanSupplier active;

    public UIButton(String text, Runnable clickAction) {
        this(TRUE, clickAction);
        add(new UIText(text));
    }

    public UIButton(UIIcon icon, Runnable clickAction) {
        this(TRUE, clickAction);
        outlineNormal = false;
        add(new UIImage(icon));
    }

    public UIButton(UIIcon activeIcon, UIIcon inactiveIcon, BooleanSupplier active, Runnable clickAction) {
        this(active, clickAction);
        outlineNormal = false;
        addTwoIcons(activeIcon, inactiveIcon, active);
    }

    public UIButton(UIIcon icon, String text, Runnable clickAction) {
        this(TRUE, clickAction);
        outlineNormal = false;
        add(new UIImage(icon));
        add(new UIText(text));
    }

    public UIButton(UIIcon activeIcon, UIIcon inactiveIcon, String text, BooleanSupplier active, Runnable clickAction) {
        this(active, clickAction);
        outlineNormal = false;
        addTwoIcons(activeIcon, inactiveIcon, active);
        add(new UIText(text));
    }

    private UIButton(BooleanSupplier active, Runnable clickAction) {
        super(HORIZONTAL, CENTER);
        this.active = active;

        // why do we need this?
        // backgroundNormal = true;

        backgroundHighlight = true;
        selectable = true;
        addLeftClickAction(clickAction);
    }

    private void addTwoIcons(UIIcon activeIcon, UIIcon inactiveIcon, BooleanSupplier active) {
        add(new UIImage(activeIcon).setVisibilitySupplier(active));
        add(new UIImage(inactiveIcon).setVisibilitySupplier(() -> !active.getAsBoolean()));
    }

    @Override
    public void update() {
        super.update();

        boolean isActive = active.getAsBoolean();
        selectable = isActive;
        backgroundHighlight = isActive;
    }
}