package sutil.ui;

import java.util.function.Supplier;

public class UILabel extends UIContainer {

    /**
     * TODO: when the textUpdater returns text containig newline characters, the
     * text is not properly split across multiple lines.
     */
    private Supplier<String> textUpdater;

    private UIText firstChild;

    private UILabel() {
        super(VERTICAL, LEFT);
        outlineNormal = false;

        zeroPadding();

        textUpdater = null;
    }

    public UILabel(String[] text) {
        this();
        if (text.length == 0) {
            firstChild = new UIText("");
            add(firstChild);
        } else {
            for (String line : text) {
                UIText child = new UIText(line);
                if (firstChild == null) {
                    firstChild = child;
                }
                add(child);
            }
        }
    }

    public UILabel(String text) {
        this(text.split("\n"));
    }

    public UILabel(Supplier<String> textUpdater) {
        this("");
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