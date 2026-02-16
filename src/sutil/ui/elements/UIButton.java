package sutil.ui.elements;

import java.util.function.Supplier;

public class UIButton extends UILabel {

    public UIButton(String text, Runnable clickAction) {
        super(text);

        init(clickAction);
    }

    public UIButton(Supplier<String> textSupplier, Runnable clickAction) {
        super(textSupplier);

        init(clickAction);
    }

    private void init(Runnable clickAction) {
        outlineNormal = true;
        backgroundNormal = true;
        backgroundHighlight = true;

        setHAlignment(CENTER);

        setLeftClickAction(clickAction);
        selectable = true;
    }
}