package sutil.ui;

import java.util.function.Supplier;

import sutil.math.SVector;

public class UIButton extends UILabel {

    public UIButton(String text, UIAction clickAction) {
        super(text);

        init(clickAction);
    }

    public UIButton(Supplier<String> textGetter, UIAction clickAction) {
        super(textGetter);

        init(clickAction);
    }

    private void init(UIAction clickAction) {
        Supplier<SVector> bg = () -> mouseAbove
                ? panel.getBackgroundHighlightColor()
                : panel.getBackgroundNormalColor();
        Supplier<SVector> ol = () -> panel.getOutlineNormalColor();
        Supplier<Double> sw = () -> panel.getStrokeWeight();
        setStyle(new UIStyle(bg, ol, sw));

        setLeftClickAction(clickAction);
        selectable = true;
    }
}