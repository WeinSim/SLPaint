package sutil.ui;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class UIDropdown extends UIContainer {

    private boolean expanded;

    private final UIFloatMenu dropdown;

    public UIDropdown(String[] options, Supplier<Integer> valueSupplier, Consumer<Integer> valueSetter) {
        this(options, valueSupplier, valueSetter, false);
    }

    public UIDropdown(String[] options, Supplier<Integer> valueSupplier, Consumer<Integer> valueSetter,
            boolean scroll) {

        super(0, 0);

        noBackground();
        withOutline();
        zeroPadding();
        backgroundHighlight = true;

        add(new UIText(() -> options[valueSupplier.get()]));

        setLeftClickAction(() -> {
            expanded = !expanded;
        });

        dropdown = new UIFloatMenu(scroll, this::isExpanded, UIText.NORMAL);
        dropdown.setCloseAction(() -> expanded = false);
        dropdown.addAnchor(UIFloatContainer.Anchor.TOP_LEFT, this, UIFloatContainer.Anchor.BOTTOM_LEFT);
        dropdown.addAnchor(UIFloatContainer.Anchor.BOTTOM_LEFT, this, UIFloatContainer.Anchor.TOP_LEFT);
        for (int i = 0; i < options.length; i++) {
            final int j = i;
            dropdown.addLabel(options[i], () -> valueSetter.accept(j));
        }

        add(dropdown);

        expanded = false;
    }

    private boolean isExpanded() {
        return expanded;
    }
}