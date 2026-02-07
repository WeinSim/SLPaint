package sutil.ui.elements;

import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

import sutil.ui.UI;

public class UILabel extends UIContainer {

    private Supplier<String> textUpdater;

    private UIText firstChild;

    public UILabel(String[] text, DoubleSupplier textSizeUpdater) {
        super(UI.VERTICAL, UI.LEFT);

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

    public UILabel(String text, DoubleSupplier textSizeUpdater) {
        this(text.split("\n"), textSizeUpdater);
    }

    public UILabel(Supplier<String> textUpdater) {
        this(textUpdater, UIText.NORMAL);
    }

    public UILabel(Supplier<String> textUpdater, DoubleSupplier textSizeUpdater) {
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