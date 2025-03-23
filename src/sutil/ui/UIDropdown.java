package sutil.ui;

import java.util.function.Consumer;
import java.util.function.Supplier;

import sutil.math.SVector;

public class UIDropdown extends UIContainer {

    private boolean expanded;

    private Supplier<Integer> valueGetter;
    private String[] options;
    private UIText text;

    private final UIFloatMenu dropdown;

    public UIDropdown(UIPanel panel, String[] options, Supplier<Integer> valueGetter, Consumer<Integer> valueSetter) {
        this(panel, options, valueGetter, valueSetter, false);
    }

    public UIDropdown(UIPanel panel, String[] options, Supplier<Integer> valueGetter, Consumer<Integer> valueSetter,
            boolean scroll) {

        super(0, 0);
        this.options = options;
        this.valueGetter = valueGetter;

        noBackground();
        withOutline();
        zeroPadding();
        backgroundHighlight = true;

        text = new UIText("");
        add(text);

        setLeftClickAction(() -> {
            expanded = !expanded;
        });

        dropdown = new UIFloatMenu(panel, () -> new SVector(0, size.y), this::isExpanded, scroll);
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

        text.setText(options[valueGetter.get()]);
    }

    @Override
    public void mousePressed(int mouseButton) {
        super.mousePressed(mouseButton);

        if (!mouseAbove() && !dropdown.mouseAbove()) {
            expanded = false;
        }
    }

    private boolean isExpanded() {
        return expanded;
    }
}