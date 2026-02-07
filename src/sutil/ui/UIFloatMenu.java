package sutil.ui;

import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;

import org.lwjgl.glfw.GLFW;
import org.lwjglx.util.vector.Vector4f;

import sutil.SUtil;

public class UIFloatMenu extends UIFloatContainer {

    private final UIContainer contents;

    private final DoubleSupplier textSizeUpdater;

    protected CMLabel expandedLabel;

    protected Runnable closeAction = () -> {
    };

    public UIFloatMenu() {
        this(false);
    }

    public UIFloatMenu(boolean scroll) {
        this(scroll, null, UIText.SMALL);
    }

    public UIFloatMenu(boolean scroll, BooleanSupplier visibilitySupplier, DoubleSupplier textSizeUpdater) {
        this.textSizeUpdater = textSizeUpdater;

        super(VERTICAL, LEFT);

        setVisibilitySupplier(visibilitySupplier);

        zeroMargin();
        zeroPadding();

        ignoreParentClipArea = true;
        clipToRoot = true;

        if (scroll) {
            UIContainer scrollArea = new UIContainer(VERTICAL, LEFT, TOP, UI.VERTICAL);
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
        addLabel(text, null, clickAction, () -> true);
    }

    // public void addLabel(String mainText, String rightText, Runnable clickAction)
    // {
    // addLabel(mainText, rightText, clickAction, () -> true);
    // }

    public void addLabel(String mainText, String rightText, Runnable clickAction, BooleanSupplier active) {
        CMLabel label = new CMLabel(mainText, rightText, active);
        label.style.setBackgroundColor(
                () -> active.getAsBoolean() && label.mouseAbove
                        ? UIColors.BACKGROUND_HIGHLIGHT.get()
                        : null);
        label.setLeftClickAction(() -> {
            if (!active.getAsBoolean())
                return;
            if (clickAction != null)
                clickAction.run();
            close();
        });
        add(label);
    }

    public void addLabel(String labelText, KeyboardShortcut shortcut) {
        int modifiers = shortcut.getModifiers();
        String rightText = "";
        if ((modifiers & GLFW.GLFW_MOD_CONTROL) != 0)
            rightText += "Ctrl + ";
        if ((modifiers & GLFW.GLFW_MOD_SHIFT) != 0)
            rightText += "Shift + ";
        if ((modifiers & GLFW.GLFW_MOD_ALT) != 0)
            rightText += "Alt + ";

        int key = shortcut.getKey();
        int scancode = GLFW.glfwGetKeyScancode(key);
        rightText += GLFW.glfwGetKeyName(key, scancode).toUpperCase();

        addLabel(labelText, rightText, shortcut::run, shortcut::isPossible);
    }

    public void addNestedContextMenu(String text, UIFloatMenu contextMenu) {
        CMLabel label = new CMLabel(text, ">", null);
        add(label);

        contextMenu.addAnchor(Anchor.TOP_LEFT, label, Anchor.TOP_RIGHT);
        contextMenu.addAnchor(Anchor.TOP_RIGHT, label, Anchor.TOP_LEFT);
        contextMenu.addAnchor(Anchor.BOTTOM_LEFT, label, Anchor.BOTTOM_RIGHT);
        contextMenu.addAnchor(Anchor.BOTTOM_RIGHT, label, Anchor.BOTTOM_LEFT);
        contextMenu.setVisibilitySupplier(() -> expandedLabel == label);

        label.style.setBackgroundColor(() -> {
            Vector4f bgColor = null;
            if (label.mouseAbove() || expandedLabel == label) {
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

    private class CMLabel extends UIContainer {

        CMLabel(String mainText, String rightText, BooleanSupplier active) {
            super(HORIZONTAL, LEFT, CENTER);

            noOutline();

            setHMarginScale(2.0);

            setHFillSize();

            UIText text = new UIText(mainText, textSizeUpdater);
            if (active != null) {
                text.setColor(SUtil.ifThenElse(active, UIColors.TEXT, UIColors.TEXT_INVALID));
            }
            add(text);

            if (rightText != null) {
                add(new UIContainer(0, 0).setHFillSize().noOutline());
                add(new UIText(rightText, textSizeUpdater).setColor(UIColors.TEXT_INVALID));
            }
        }

        @Override
        public void update() {
            super.update();

            if (mouseAbove()) {
                expandedLabel = this;
            }
        }
    }
}