package sutil.ui.elements;

public class UIIconButton extends UIContainer {

    public UIIconButton(String name, Runnable clickAction) {
        super(VERTICAL, CENTER);

        backgroundHighlight = true;
        outlineNormal = false;

        addLeftClickAction(clickAction);

        add(new UIIcon(name));
    }
}