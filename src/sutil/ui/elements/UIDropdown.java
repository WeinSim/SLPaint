package sutil.ui.elements;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public class UIDropdown extends UIContainer {

    private boolean expanded;

    private final UIFloatMenu dropdown;

    public UIDropdown(String text) {
        this(() -> text, false);
    }

    public UIDropdown(Supplier<String> textUpdater, boolean scroll) {
        super(CENTER, CENTER);

        noBackground();
        withOutline();
        zeroPadding();
        backgroundHighlight = true;

        selectable = true;

        add(new UIText(textUpdater));

        addLeftClickAction(() -> expanded = !expanded);

        dropdown = new UIFloatMenu(() -> expanded, () -> expanded = false, scroll, UIText.NORMAL);
        dropdown.addAnchor(UIFloatContainer.Anchor.TOP_LEFT, UIFloatContainer.Anchor.BOTTOM_LEFT);
        dropdown.addAnchor(UIFloatContainer.Anchor.BOTTOM_LEFT, UIFloatContainer.Anchor.TOP_LEFT);

        add(dropdown);

        expanded = false;
    }

    public UIDropdown(String[] options, IntSupplier valueSupplier, IntConsumer valueSetter) {
        this(options, valueSupplier, valueSetter, false);
    }

    public UIDropdown(String[] options, IntSupplier valueSupplier, IntConsumer valueSetter,
            boolean scroll) {

        this(() -> options[valueSupplier.getAsInt()], scroll);

        for (int i = 0; i < options.length; i++) {
            final int j = i;
            addLabel(options[i], () -> valueSetter.accept(j));
        }
    }

    public void addLabel(String text, Runnable clickAction) {
        dropdown.addLabel(text, clickAction);
    }
}