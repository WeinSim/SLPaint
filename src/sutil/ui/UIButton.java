package sutil.ui;

import sutil.math.SVector;

public class UIButton extends UILabel {

    public UIButton(String text, UIAction clickAction) {
        super(text);

        init(clickAction);
    }

    public UIButton(UIGetter<String> textGetter, UIAction clickAction) {
        super(textGetter);

        init(clickAction);
    }

    private void init(UIAction clickAction) {
        UIGetter<SVector> bg = () -> mouseAbove
                ? panel.getBackgroundHighlightColor()
                : panel.getBackgroundNormalColor();
        UIGetter<SVector> ol = () -> panel.getOutlineNormalColor();
        UIGetter<Double> sw = () -> panel.getStrokeWeight();
        setStyle(new UIStyle(bg, ol, sw));

        setClickAction(clickAction);
        selectable = true;
    }
}