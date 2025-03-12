package sutil.ui;

import java.util.function.Consumer;
import java.util.function.Supplier;

import sutil.math.SVector;

public class UIDropdown extends UIContainer {

    private boolean expanded;
    private UIFloatContainer dropdown;

    public UIDropdown(String[] options, Supplier<Integer> valueGetter, Consumer<Integer> valueSetter) {
        super(0, 0);

        zeroMargin();
        noBackground();
        withOutline();
        zeroPadding();

        UILabel top = new UILabel(() -> options[valueGetter.get()]);
        top.setClickAction(() -> {
            if (expanded)
                minimize();
            else
                expand();
        });
        top.backgroundNormal = false;
        top.backgroundHighlight = true;
        add(top);

        dropdown = new UIFloatContainer(
                VERTICAL, LEFT,
                () -> getAbsolutePosition().add(new SVector(0, size.y)));
        dropdown.zeroMargin().zeroPadding().withBackground();
        // dropdown.add(new UISeparator().withBackground());
        for (int i = 0; i < options.length; i++) {
            UILabel label = new UILabel(options[i]);
            label.backgroundNormal = true;
            label.backgroundHighlight = true;
            final int j = i;
            label.setClickAction(() -> {
                valueSetter.accept(j);
                minimize();
            });
            label.setFillSize();
            dropdown.add(label);
        }

        expanded = false;
    }

    @Override
    public void update(SVector mouse) {
        super.update(mouse);
    }

    @Override
    public void mousePressed(SVector mouse) {
        super.mousePressed(mouse);

        if (!mouseAbove()) {
            minimize();
        }
    }

    public void expand() {
        panel.queueEvent(() -> {
            if (expanded) {
                return;
            }
            expanded = true;
            // dropdown.getPosition().set(getAbsolutePosition().add(new SVector(0,
            // size.y)));
            panel.addFloatContainer(dropdown);
        });
    }

    public void minimize() {
        panel.queueEvent(() -> {
            if (!expanded) {
                return;
            }
            expanded = false;
            panel.removeFloatContainer(dropdown);
        });
    }
}