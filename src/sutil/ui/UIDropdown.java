package sutil.ui;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class UIDropdown extends UIContainer {

    private boolean expanded;

    private Supplier<Integer> valueSupplier;
    private String[] options;
    private UIText text;

    private final UIFloatMenu dropdown;

    public UIDropdown(String[] options, Supplier<Integer> valueSupplier, Consumer<Integer> valueSetter) {
        this(options, valueSupplier, valueSetter, false);
    }

    public UIDropdown(String[] options, Supplier<Integer> valueSupplier, Consumer<Integer> valueSetter,
            boolean scroll) {

        super(0, 0);
        this.options = options;
        this.valueSupplier = valueSupplier;

        noBackground();
        withOutline();
        zeroPadding();
        backgroundHighlight = true;

        text = new UIText("");
        add(text);

        setLeftClickAction(() -> {
            expanded = !expanded;
        });

        dropdown = new UIFloatMenu(this::isExpanded, scroll);
        dropdown.addAnchor(UIFloatContainer.Anchor.TOP_LEFT, this, UIFloatContainer.Anchor.BOTTOM_LEFT);
        dropdown.addAnchor(UIFloatContainer.Anchor.BOTTOM_LEFT, this, UIFloatContainer.Anchor.TOP_LEFT);
        UIAction[] clickActions = new UIAction[options.length];
        for (int i = 0; i < clickActions.length; i++) {
            final int j = i;
            dropdown.addLabel(options[i], () -> {
                valueSetter.accept(j);
                expanded = false;
            });
        }

        add(dropdown);

        expanded = false;
    }

    @Override
    public void update() {
        super.update();

        text.setText(options[valueSupplier.get()]);
    }

    @Override
    public void mousePressed(int mouseButton, int mods) {
        super.mousePressed(mouseButton, mods);

        if (!mouseAbove() && !dropdown.mouseAbove()) {
            expanded = false;
        }
    }

    private boolean isExpanded() {
        return expanded;
    }
}