package sutil.ui;

import java.util.ArrayList;
import java.util.function.Supplier;

public class UILabel extends UIContainer {

    /**
     * TODO
     * As it is currently implemented, there is a bug with the textUpdater:
     * When a UILabel is initialized with a textUpdater, then calling
     * setText(String) will not work because the textUpdater is still in action.
     * You would have to call {@code setText((Supplier<String>) null)} separately.
     */
    private Supplier<String> textUpdater;

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

    public UILabel(Supplier<String> textUpdater) {
        this();
        this.textUpdater = textUpdater;
    }

    @Override
    public void update() {
        super.update();

        if (textUpdater != null) {
            setText(textUpdater.get().split("\n"));
        }
    }

    public void setText(String text) {
        setText(text.split("\n"));
    }

    public void setText(String[] text) {
        clearChildren();
        for (String line : text) {
            add(new UIText(line));
        }
    }

    public void setText(ArrayList<String> text) {
        for (String line : text) {
            add(new UIText(line));
        }
    }

    public void setText(Supplier<String> textUpdater) {
        this.textUpdater = textUpdater;
    }
}