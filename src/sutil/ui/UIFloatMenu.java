package sutil.ui;

import java.util.function.Supplier;

import org.lwjglx.util.vector.Vector4f;

public class UIFloatMenu extends UIFloatContainer {

    private final UIContainer contents;

    private final Supplier<Double> textSizeUpdater;

    protected CMLabel expandedLabel;

    protected Runnable closeAction = () -> {
    };

    public UIFloatMenu() {
        this(false);
    }

    public UIFloatMenu(boolean scroll) {
        this(scroll, null, UIText.SMALL);
    }

    public UIFloatMenu(boolean scroll, Supplier<Boolean> visibilitySupplier, Supplier<Double> textSizeUpdater) {
        this.textSizeUpdater = textSizeUpdater;

        super(VERTICAL, LEFT);

        setVisibilitySupplier(visibilitySupplier);

        zeroMargin();
        zeroPadding();

        ignoreParentClipArea = true;
        clipToRoot = true;

        if (scroll) {
            UIContainer scrollArea = new UIContainer(VERTICAL, LEFT, TOP, UIContainer.VERTICAL);
            scrollArea.setVFixedSize(400).zeroMargin().zeroPadding();
            contents = scrollArea;
            add(scrollArea.addScrollbars(), true);
        } else {
            contents = this;
        }
    }

    @Override
    public void mousePressed(int mouseButton, int mods) {
        super.mousePressed(mouseButton, mods);

        // Kind of a dumb hack.
        // The first check is neccessary because it prevents the following situation:
        // A UIContextMenu and one of its attached float menues are open.
        // The user clicks somewhere in the parent menu. Because the mouse is not abovew
        // the child menu, it closes (and also causes the parent to close).
        if (!(parent instanceof UIFloatContainer) && shouldClose(this)) {
            close();
        }
    }

    private boolean shouldClose(UIElement element) {
        if (element.mouseAbove)
            return false;
        if (element instanceof UIContainer container) {
            for (UIElement child : container.getChildren()) {
                if (!shouldClose(child))
                    return false;
            }
        }
        return true;
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

    public void addLabel(String text, Runnable clickAction) {
        CMLabel label = new CMLabel(text, this);
        label.backgroundHighlight = true;
        label.setLeftClickAction(() -> {
            if (clickAction != null)
                clickAction.run();
            close();
        });
        label.setHFillSize();
        add(label);
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
                bgColor = UIColors.BACKGROUND_NORMAL.get();
            }
            if ((label.mouseAbove() || expandedLabel == label) && label.backgroundHighlight) {
                bgColor = UIColors.BACKGROUND_HIGHLIGHT.get();
            }
            return bgColor;
        });

        contextMenu.setCloseAction(() -> {
            expandedLabel = null;
            close();
        });

        add(contextMenu);
    }

    public void addSeparator() {
        UIContainer container = new UIContainer(VERTICAL, 0);
        container.setVMarginScale(1.0).setHMarginScale(0.0).setHFillSize().noOutline();
        container.add(new UISeparator());
        add(container);
    }

    public void close() {
        closeAction.run();
    }

    public void setCloseAction(Runnable closeAction) {
        this.closeAction = closeAction;
    }

    private class CMLabel extends UILabel {

        final UIFloatMenu contextMenu;

        CMLabel(String text, UIFloatMenu contextMenu) {
            super(text, textSizeUpdater);

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