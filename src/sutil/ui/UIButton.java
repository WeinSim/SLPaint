package sutil.ui;

import java.util.function.Supplier;

public class UIButton extends UILabel {

    public UIButton(String text, UIAction clickAction) {
        super(text);

        init(clickAction);
    }

    public UIButton(Supplier<String> textSupplier, UIAction clickAction) {
        super(textSupplier);

        init(clickAction);
    }

    private void init(UIAction clickAction) {
        outlineNormal = true;
        backgroundNormal = true;
        backgroundHighlight = true;

        setLeftClickAction(clickAction);
        selectable = true;
    }
}