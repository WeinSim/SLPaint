package sutil.ui;

import java.util.ArrayList;

import sutil.math.SVector;

public class UILabel extends UIContainer {

    /**
     * As it is currently implemented, there is a bug with the textUpdater:
     * When a UILabel is initialized with a textUpdater, then calling
     * setText(String) will not work because the textUpdater is still in action.
     * You would have to call {@code setText((UIGetter<String>) null)} separately.
     */
    private UIGetter<String> textUpdater;

    private UILabel() {
        super(VERTICAL, LEFT);
        outlineNormal = false;

        zeroPadding();

        textUpdater = null;
    }

    public UILabel(String text) {
        this();
        setText(text);
    }

    public UILabel(String[] text) {
        this();
        setText(text);
    }

    public UILabel(ArrayList<String> text) {
        this();
        setText(text);
    }

    public UILabel(UIGetter<String> textUpdater) {
        this();
        this.textUpdater = textUpdater;
    }

    @Override
    public void update(SVector mouse) {
        if (textUpdater != null) {
            setText(textUpdater.get().split("\n"));
        }
        super.update(mouse);
    }

    public void setText(String text) {
        setText(text.split("\n"));
    }

    public void setText(String[] text) {
        children.clear();
        for (String line : text) {
            add(new UIText(line));
        }
    }

    public void setText(ArrayList<String> text) {
        for (String line : text) {
            add(new UIText(line));
        }
    }

    public void setText(UIGetter<String> textUpdater) {
        this.textUpdater = textUpdater;
    }
}