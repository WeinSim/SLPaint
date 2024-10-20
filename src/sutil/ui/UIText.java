package sutil.ui;

import sutil.math.SVector;

public class UIText extends UIElement {

    private String text;

    private UIGetter<String> textUpdater;

    public UIText(String text) {
        this.text = text;
        textUpdater = null;
    }

    public UIText(UIGetter<String> textUpdater) {
        text = "";
        this.textUpdater = textUpdater;
    }

    @Override
    public void setMinSize() {
        double tw = panel.textWidth(getText());
        size = new SVector(tw, panel.getTextSize());
    }

    @Override
    public void update(SVector mouse) {
        if (textUpdater != null) {
            text = textUpdater.get();
        }
        super.update(mouse);
    }

    public String getText() {
        return textUpdater != null ? textUpdater.get() : text;
    }

    public void setText(String text) {
        this.text = text;
        textUpdater = null;
    }

    public void setTextUpdater(UIGetter<String> textUpdater) {
        text = null;
        this.textUpdater = textUpdater;
    }
}