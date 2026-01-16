package sutil.ui;

import java.util.function.Supplier;

public class UILabel extends UIContainer {

    private Supplier<String> textUpdater;

    private UIText firstChild;

    public UILabel(String[] text, boolean smallText) {
        super(VERTICAL, LEFT);

        outlineNormal = false;
        zeroPadding();

        textUpdater = null;

        if (text.length == 0) {
            firstChild = new UIText("", smallText);
            add(firstChild);
        } else {
            for (String line : text) {
                UIText child = new UIText(line, smallText);
                if (firstChild == null) {
                    firstChild = child;
                }
                add(child);
            }
        }
    }

    public UILabel(String text) {
        this(text, false);
    }

    public UILabel(String text, boolean smallText) {
        this(text.split("\n"), smallText);
    }

    public UILabel(Supplier<String> textUpdater) {
        this(textUpdater, false);
    }

    public UILabel(Supplier<String> textUpdater, boolean smallText) {
        this("", smallText);
        this.textUpdater = textUpdater;
    }

    @Override
    public void update() {
        super.update();

        if (textUpdater != null) {
            firstChild.setText(textUpdater.get());
        }
    }
}