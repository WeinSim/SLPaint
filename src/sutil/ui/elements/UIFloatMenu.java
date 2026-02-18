package sutil.ui.elements;

import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;

import org.lwjgl.glfw.GLFW;

import sutil.SUtil;
import sutil.ui.KeyboardShortcut;
import sutil.ui.UI;
import sutil.ui.UIColors;

/*
 * Appearances of UIFloatMenu:
 * Child of UIDropdown:
 *   boolean expanded
 *   setCloseAction(expanded = !expanded)
 * Grandhild of UIMenuBar:
 *   UIFloatMenu expandedMenu
 *   setCloseAction(expandedMenu = null)
 * UIContextMenu:
 *   boolean expanded
 *     (in the float menu itself!)
 *   expandedLabel = null;
 *   close();
 */
public class UIFloatMenu extends UIFloatContainer {

    private final UIContainer contents;

    private final DoubleSupplier textSizeUpdater;

    protected CMLabel expandedLabel;

    protected Runnable closeAction;

    public UIFloatMenu(BooleanSupplier visibilitySupplier, Runnable closeAction) {
        this(visibilitySupplier, closeAction, false, UIText.SMALL);
    }

    public UIFloatMenu(BooleanSupplier visibilitySupplier, Runnable closeAction, boolean scroll) {
        this(visibilitySupplier, closeAction, scroll, UIText.SMALL);
    }

    public UIFloatMenu(BooleanSupplier visibilitySupplier, Runnable closeAction, boolean scroll,
            DoubleSupplier textSizeUpdater) {

        super(VERTICAL, LEFT);

        setVisibilitySupplier(visibilitySupplier);
        this.closeAction = closeAction;
        this.textSizeUpdater = textSizeUpdater;

        zeroMargin();
        zeroPadding();

        relativeLayer = 1;
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
        // The user clicks somewhere in the parent menu. Because the mouse is not above
        // the child menu, it closes (and also causes the parent to close).
        if (!(parent instanceof UIFloatContainer) && shouldClose(this)) {
            close();
        }
    }

    // returns true if this element of one of its children has mouseAbove set.
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
        if (normalAdd)
            super.add(child);
        else
            contents.add(child);
    }

    @Override
    public void add(UIElement child) {
        add(child, contents == this || contents == null);
    }

    public void addLabel(String text, Runnable clickAction) {
        addLabel(text, null, clickAction, () -> true);
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

    private void addLabel(String mainText, String rightText, Runnable clickAction, BooleanSupplier active) {
        CMLabel label = new CMLabel(mainText, rightText, active);
        label.style.setBackgroundColor(
                () -> active.getAsBoolean() && label.mouseAbove()
                        ? UIColors.BACKGROUND_HIGHLIGHT.get()
                        : null);
        if (clickAction != null)
            label.setLeftClickAction(() -> {
                if (!active.getAsBoolean())
                    return;
                clickAction.run();
                close();
            });
        add(label);
    }

    public UIFloatMenu addNestedMenu(String text) {
        return addNestedMenu(text, false);
    }

    public UIFloatMenu addNestedMenu(String text, boolean scroll) {
        CMLabel label = new CMLabel(text, ">", null);
        label.style.setBackgroundColor(
                () -> label.mouseAbove() || expandedLabel == label
                        ? UIColors.BACKGROUND_HIGHLIGHT.get()
                        : null);

        UIFloatMenu menu = new UIFloatMenu(
                () -> expandedLabel == label,
                () -> {
                    expandedLabel = null;
                    close();
                },
                scroll);
        menu.addAnchor(Anchor.TOP_LEFT, Anchor.TOP_RIGHT);
        menu.addAnchor(Anchor.TOP_RIGHT, Anchor.TOP_LEFT);
        menu.addAnchor(Anchor.BOTTOM_LEFT, Anchor.BOTTOM_RIGHT);
        menu.addAnchor(Anchor.BOTTOM_RIGHT, Anchor.BOTTOM_LEFT);

        label.add(menu);

        add(label);

        return menu;
    }

    public void addSeparator() {
        UIContainer container = new UIContainer(VERTICAL, 0);
        container.setVMarginScale(1.0).setHMarginScale(0.0).setHFillSize().noOutline();
        container.add(new UISeparator());
        add(container);
    }

    /**
     * @implNote This method is not guaranteed to be called whenever the menu
     *           closes. A close could e.g. also be triggered by a UIMenuBar setting
     *           {@code expandedMenu} to {@code null}.
     */
    public void close() {
        closeAction.run();
    }

    private class CMLabel extends UIContainer {

        CMLabel(String mainText, String rightText, BooleanSupplier active) {
            super(HORIZONTAL, LEFT, CENTER);

            noOutline();
            setHMarginScale(2.0);
            setHFillSize();

            UIText text = new UIText(mainText, textSizeUpdater);
            if (active != null)
                text.setColor(SUtil.ifThenElse(active, UIColors.TEXT, UIColors.TEXT_INVALID));
            add(text);

            if (rightText != null) {
                add(new UIContainer(0, 0).setVMarginScale(0).setHFillSize().noOutline());
                add(new UIText(rightText, textSizeUpdater).setColor(UIColors.TEXT_INVALID));
            }
        }

        @Override
        public void update() {
            super.update();

            if (mouseAbove())
                expandedLabel = this;
        }
    }
}