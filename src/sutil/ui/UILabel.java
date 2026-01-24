package sutil.ui;

import java.util.function.Supplier;

public class UILabel extends UIContainer {

    private Supplier<String> textUpdater;

    private UIText firstChild;

    public UILabel(String[] text, Supplier<Double> textSizeUpdater) {
        super(VERTICAL, LEFT);

        outlineNormal = false;
        zeroPadding();

        textUpdater = null;

        if (text.length == 0) {
            firstChild = new UIText("", textSizeUpdater);
            add(firstChild);
        } else {
            for (String line : text) {
                UIText child = new UIText(line, textSizeUpdater);
                if (firstChild == null) {
                    firstChild = child;
                }
                add(child);
            }
        }
    }

    public UILabel(String text) {
        this(text, UIText.NORMAL);
    }

    public UILabel(String text, Supplier<Double> textSizeUpdater) {
        this(text.split("\n"), textSizeUpdater);
    }

    public UILabel(Supplier<String> textUpdater) {
        this(textUpdater, UIText.NORMAL);
    }

    public UILabel(Supplier<String> textUpdater, Supplier<Double> textSizeUpdater) {
        this("", textSizeUpdater);
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