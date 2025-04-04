package sutil.ui;

import java.util.function.Supplier;

import sutil.math.SVector;

public class UIFloatMenu extends UIFloatContainer {

    private final UIContainer contents;

    protected CMLabel expandedLabel;

    public UIFloatMenu(UIPanel panel, Supplier<Boolean> visibilitySupplier,
            boolean scroll) {
        super(panel, VERTICAL, LEFT);

        setVisibilitySupplier(visibilitySupplier);

        zeroMargin();
        zeroPadding();

        if (scroll) {
            UIContainer scrollArea = new UIContainer(VERTICAL, LEFT, TOP, UIContainer.VERTICAL);
            scrollArea.setVFixedSize(200).zeroMargin().zeroPadding();
            contents = scrollArea;
            add(scrollArea.addScrollbars());
        } else {
            contents = this;
        }
    }

    public UILabel addLabel(String text, UIAction clickAction) {
        CMLabel label = new CMLabel(text, this);
        label.backgroundHighlight = true;
        label.setLeftClickAction(clickAction);
        label.setHFillSize();
        contents.add(label);
        return label;
    }

    public void addSeparator() {
        contents.add(new UISeparator());
    }

    public void addNestedContextMenu(String text, UIFloatMenu contextMenu) {
        CMLabel label = new CMLabel(text, this);
        label.backgroundHighlight = true;
        label.setHFillSize();
        contents.add(label);

        contextMenu.addAttachPoint(TOP_LEFT, label, TOP_RIGHT);
        contextMenu.addAttachPoint(TOP_RIGHT, label, TOP_LEFT);
        contextMenu.addAttachPoint(BOTTOM_LEFT, label, BOTTOM_RIGHT);
        contextMenu.addAttachPoint(BOTTOM_RIGHT, label, BOTTOM_LEFT);
        contextMenu.setVisibilitySupplier(() -> expandedLabel == label);

        label.style.backgroundColorSupplier = () -> {
            SVector bgColor = null;
            if (backgroundNormal) {
                bgColor = panel.getBackgroundNormalColor();
            }
            if ((label.mouseAbove() || expandedLabel == label) && label.backgroundHighlight) {
                bgColor = panel.getBackgroundHighlightColor();
            }
            return bgColor;
        };

        add(contextMenu);
    }

    private static class CMLabel extends UILabel {

        final UIFloatMenu contextMenu;

        CMLabel(String text, UIFloatMenu contextMenu) {
            super(text);

            this.contextMenu = contextMenu;
        }

        @Override
        public void update() {
            super.update();

            if (mouseAbove()) {
                contextMenu.expandedLabel = this;
            }
        }
    }
}