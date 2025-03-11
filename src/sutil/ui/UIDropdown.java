package sutil.ui;

import sutil.math.SVector;

public class UIDropdown extends UIContainer {

    // private String[] options;

    private String selectedOption;

    private boolean expanded;
    private UIContainer dropdown;

    public UIDropdown(String[] options) {
        super(0, 0);

        // this.options = options;
        selectedOption = options[0];

        zeroMargin();
        noBackground();
        withOutline();
        zeroPadding();

        UILabel top = new UILabel(() -> selectedOption);
        top.setClickAction(() -> {
            if (expanded)
                minimize();
            else
                expand();
        });
        top.backgroundNormal = false;
        top.backgroundHighlight = true;
        add(top);

        dropdown = new UIContainer(VERTICAL, LEFT).zeroMargin().zeroPadding();
        dropdown.noOutline();
        dropdown.add(new UISeparator().withBackground());
        for (String opt : options) {
            UILabel label = new UILabel(opt);
            label.backgroundNormal = true;
            label.backgroundHighlight = true;
            label.setClickAction(() -> {
                selectedOption = opt;
                minimize();
            });
            dropdown.add(label);
        }

        expanded = false;
    }

    public void expand() {
        panel.queueEvent(() -> {
            if (expanded) {
                return;
            }
            expanded = true;
            dropdown.getPosition().set(getAbsolutePosition().add(new SVector(0, size.y)));
            panel.addFloatElement(dropdown);
        });
    }

    public void minimize() {
        panel.queueEvent(() -> {
            if (!expanded) {
                return;
            }
            expanded = false;
            panel.removeFloatElement(dropdown);
        });
    }
}