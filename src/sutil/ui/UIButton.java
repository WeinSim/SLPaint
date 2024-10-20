package sutil.ui;

import sutil.math.SVector;

public class UIButton extends UILabel {

    public UIButton(String text, UIAction clickAction) {
        super(text);

        UIGetter<SVector> bg = () -> mouseAbove
                ? panel.getBackgroundHighlightColor()
                : panel.getBackgroundNormalColor();
        UIGetter<SVector> ol = () -> panel.getOutlineNormalColor();
        UIGetter<Double> sw = () -> 1.0;
        setStyle(new UIStyle(bg, ol, sw));

        setClickAction(clickAction);
        selectable = true;
    }
}