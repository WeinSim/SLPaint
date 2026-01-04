package sutil.ui;

import java.util.function.Supplier;

import org.lwjglx.util.vector.Vector4f;

public class UIFloatMenu extends UIFloatContainer {

    private final UIContainer contents;

    protected CMLabel expandedLabel;

    public UIFloatMenu(boolean scroll) {
        this(null, scroll);
    }

    public UIFloatMenu(Supplier<Boolean> visibilitySupplier, boolean scroll) {
        super(VERTICAL, LEFT);

        setVisibilitySupplier(visibilitySupplier);

        zeroMargin();
        zeroPadding();

        if (scroll) {
            UIContainer scrollArea = new UIContainer(VERTICAL, LEFT, TOP, UIContainer.VERTICAL);
            scrollArea.setVFixedSize(400).zeroMargin().zeroPadding();
            contents = scrollArea;
            add(scrollArea.addScrollbars(), true);
        } else {
            contents = this;
        }
    }

    private void add(UIElement child, boolean normalAdd) {
        if (normalAdd) {
            super.add(child);
        } else {
            contents.add(child);
        }
    }

    @Override
    public void add(UIElement child) {
        add(child, contents == this || contents == null);
    }

    public UILabel addLabel(String text, UIAction clickAction) {
        CMLabel label = new CMLabel(text, this);
        label.backgroundHighlight = true;
        label.setLeftClickAction(clickAction);
        label.setHFillSize();
        add(label);
        return label;
    }

    public void addNestedContextMenu(String text, UIFloatMenu contextMenu) {
        CMLabel label = new CMLabel(text, this);
        label.backgroundHighlight = true;
        label.setHFillSize();
        add(label);

        contextMenu.addAnchor(Anchor.TOP_LEFT, label, Anchor.TOP_RIGHT);
        contextMenu.addAnchor(Anchor.TOP_RIGHT, label, Anchor.TOP_LEFT);
        contextMenu.addAnchor(Anchor.BOTTOM_LEFT, label, Anchor.BOTTOM_RIGHT);
        contextMenu.addAnchor(Anchor.BOTTOM_RIGHT, label, Anchor.BOTTOM_LEFT);
        contextMenu.setVisibilitySupplier(() -> expandedLabel == label);

        label.style.setBackgroundColor(() -> {
            Vector4f bgColor = null;
            if (backgroundNormal) {
                bgColor = panel.getBackgroundNormalColor();
            }
            if ((label.mouseAbove() || expandedLabel == label) && label.backgroundHighlight) {
                bgColor = panel.getBackgroundHighlightColor();
            }
            return bgColor;
        });

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