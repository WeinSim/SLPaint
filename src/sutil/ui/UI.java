package sutil.ui;

import static org.lwjgl.glfw.GLFW.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;

import org.lwjglx.util.vector.Vector4f;

import sutil.math.SVector;
import sutil.ui.elements.UIContainer;
import sutil.ui.elements.UIElement;
import sutil.ui.elements.UIModalDialog;
import sutil.ui.elements.UIRoot;

public abstract class UI {

    /**
     * UIContainer alignments
     */
    public static final int LEFT = 0,
            TOP = 0,
            CENTER = 1,
            RIGHT = 2,
            BOTTOM = 2;

    /**
     * UIContainer orientations and scroll directions
     */
    public static final int VERTICAL = 0,
            HORIZONTAL = 1,
            NONE = 2,
            BOTH = 3;

    public static final int NUM_LAYERS = 256;
    public static final int MODAL_DIALOG_LAYER = NUM_LAYERS - 1;

    /**
     * UIModalDialog dialog types
     */
    public static final int YES_NO_DIALOG = 0,
            OK_CANCEL_DIALOG = 1,
            YES_NO_CANCEL_DIALOG = 2,
            INFO_DIALOG = 3;

    /**
     * UIModalDialog return codes
     */
    public static final int YES_OPTION = 0,
            OK_OPTION = 0,
            NO_OPTION = 1,
            CANCEL_OPTION = 2,
            CLOSED_OPTION = 3,
            INVALID_OPTION = -1;

    private static UI context = null;

    protected String defaultFontName = "FreeMonoBold";

    private double uiScale = 1.0;

    protected double mouseWheelSensitivity = 100;

    protected UIRoot root;
    private SVector rootSize;

    private UIElement selectedElement;

    private boolean dragging;

    /**
     * Contains the flags of the currently held down modifier keys (Shift, Control,
     * Alt).
     */
    private int modifiers;
    private boolean leftMousePressed;
    private boolean rightMousePressed;

    private LinkedList<Runnable> eventQueue;

    public UI(double uiScale, SVector initialRootSize) {
        setContext(this);

        selectedElement = null;
        dragging = false;
        modifiers = 0;
        leftMousePressed = false;
        rightMousePressed = false;

        eventQueue = new LinkedList<>();

        this.uiScale = uiScale;

        root = new UIRoot(VERTICAL, LEFT);
        root.zeroMargin().zeroPadding().noOutline().withBackground();
        root.setFixedSize(initialRootSize);
        rootSize = new SVector(initialRootSize);

        init();
    }

    protected abstract void init();

    public void update(SVector mousePos, boolean focus) {
        while (!eventQueue.isEmpty())
            eventQueue.removeFirst().run();

        root.updateVisibility();

        // This could potentially cause some weird behavior if the selected element's
        // unselect method immediately undoes the action that made it invisible in the
        // first place
        if (selectedElement != null && !selectedElement.isVisible())
            select(null);

        root.updateMousePosition(mousePos);
        root.updateMouseAbove(!dragging);

        // The dragging variable lags one frame behind (because it is being used before
        // it is being set)
        dragging = false;
        root.update();

        root.updateSize();
    }

    public void keyPressed(int key, int mods) {
        modifiers |= switch (key) {
            case GLFW_KEY_LEFT_CONTROL, GLFW_KEY_RIGHT_CONTROL -> GLFW_MOD_CONTROL;
            case GLFW_KEY_LEFT_SHIFT, GLFW_KEY_RIGHT_SHIFT -> GLFW_MOD_SHIFT;
            case GLFW_KEY_LEFT_ALT, GLFW_KEY_RIGHT_ALT -> GLFW_MOD_ALT;
            default -> 0;
        };

        queueEvent(() -> {
            switch (key) {
                case GLFW_KEY_TAB -> cycleSelectedElement((mods & GLFW_MOD_SHIFT) != 0);
                case GLFW_KEY_ESCAPE -> select(null);
            }
            // root.keyPressed(key, mods);

            keyPressed(root, key, mods);
        });
    }

    private void keyPressed(UIElement element, int key, int mods) {
        element.keyPressed(key, mods);
        if (element instanceof UIContainer container) {
            for (UIElement child : container.getSoloChildren()) {
                keyPressed(child, key, mods);
            }
        }
    }

    public void keyReleased(int key, int mods) {
        modifiers ^= 0xFFFFFFFF;
        modifiers |= switch (key) {
            case GLFW_KEY_LEFT_CONTROL, GLFW_KEY_RIGHT_CONTROL -> GLFW_MOD_CONTROL;
            case GLFW_KEY_LEFT_SHIFT, GLFW_KEY_RIGHT_SHIFT -> GLFW_MOD_SHIFT;
            case GLFW_KEY_LEFT_ALT, GLFW_KEY_RIGHT_ALT -> GLFW_MOD_ALT;
            default -> 0;
        };
        modifiers ^= 0xFFFFFFFF;
    }

    public void mousePressed(int mouseButton, int mods) {
        queueEvent(() -> {
            switch (mouseButton) {
                case GLFW_MOUSE_BUTTON_LEFT -> leftMousePressed = true;
                case GLFW_MOUSE_BUTTON_RIGHT -> rightMousePressed = true;
            }
            if (selectedElement != null && !selectedElement.mouseAbove()) {
                select(null);
            }
            mousePressed(root, mouseButton, mods);
        });
    }

    private void mousePressed(UIElement element, int mouseButton, int mods) {
        element.mousePressed(mouseButton, mods);
        if (element instanceof UIContainer container) {
            for (UIElement child : container.getSoloChildren()) {
                mousePressed(child, mouseButton, mods);
            }
        }
    }

    public void mouseReleased(int mouseButton, int mods) {
        queueEvent(() -> {
            switch (mouseButton) {
                case GLFW_MOUSE_BUTTON_LEFT -> leftMousePressed = false;
                case GLFW_MOUSE_BUTTON_RIGHT -> rightMousePressed = false;
            }
            // root.mouseReleased(mouseButton, mods);
            mouseReleased(root, mouseButton, mods);
        });
    }

    private void mouseReleased(UIElement element, int mouseButton, int mods) {
        element.mouseReleased(mouseButton, mods);
        if (element instanceof UIContainer container) {
            for (UIElement child : container.getSoloChildren()) {
                mouseReleased(child, mouseButton, mods);
            }
        }
    }

    public void charInput(char c) {
        // queueEvent(() -> root.charInput(c));
        queueEvent(() -> charInput(root, c));
    }

    private void charInput(UIElement element, char c) {
        element.charInput(c);
        if (element instanceof UIContainer container) {
            for (UIElement child : container.getSoloChildren()) {
                charInput(child, c);
            }
        }
    }

    public void mouseWheel(SVector scroll) {
        // queueEvent(() -> root.mouseWheel(scroll.copy().scale(mouseWheelSensitivity),
        // mousePos, modifiers));
        queueEvent(() -> mouseWheel(root, scroll.copy().scale(mouseWheelSensitivity), modifiers));
    }

    private boolean mouseWheel(UIElement element, SVector scroll, int mods) {
        if (element instanceof UIContainer container) {
            for (UIElement child : container.getSoloChildren()) {
                if (mouseWheel(child, scroll, mods)) {
                    return true;
                }
            }
        }

        return element.mouseWheel(scroll, mods);
    }

    public static void queueEvent(Runnable action) {
        context.eventQueue.add(action);
    }

    public static int showModalDialog(String title, String text, int dialogType) {
        return context.showModalDialogImpl(title, text, dialogType);
    }

    private int showModalDialogImpl(String title, String text, int dialogType) {
        if (root.hasModalDialog())
            // throw new RuntimeException("Unable to show modal dialog if another one is
            // already active");
            // return INVALID_OPTION;
            UI.cancelActiveModalDialog();

        CompletableFuture<Integer> future = new CompletableFuture<>();
        queueEvent(() -> {
            select(null);
            root.add(new UIModalDialog(title, text, dialogType, future));
        });
        try {
            return future.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void cancelActiveModalDialog() {
        context.cancelActiveModalDialogImpl();
    }

    private void cancelActiveModalDialogImpl() {
        UIModalDialog dialog = root.getModalDialog();
        if (dialog == null)
            return;
        dialog.cancel();
    }

    private void cycleSelectedElement(boolean backwards) {
        ArrayList<UIElement> elements = getSelectableElements();
        if (elements.isEmpty())
            return;
        if (selectedElement == null) {
            select(backwards ? elements.getLast() : elements.getFirst());
        } else {
            int oldIndex = elements.indexOf(selectedElement);
            int newIndex = (oldIndex + (backwards ? -1 : 1) + elements.size()) % elements.size();
            select(elements.get(newIndex));
        }
    }

    private ArrayList<UIElement> getSelectableElements() {
        return root.getSelectableElements();
    }

    public int getCursorShape() {
        Integer shape = root.getCursorShape();
        return shape == null ? GLFW_ARROW_CURSOR : shape;
    }

    // Statically available methods

    public static void select(UIElement element) {
        context.selectImpl(element);
    }

    private void selectImpl(UIElement element) {
        if (selectedElement != null)
            selectedElement.unselect();
        selectedElement = element;
        if (element != null)
            element.select();
    }

    public static void setDragging() {
        context.dragging = true;
    }

    public static double textWidth(String text, double textSize, String fontName) {
        return textWidth(text, textSize, fontName, text.length());
    }

    public static double textWidth(String text, double textSize, String fontName, int len) {
        return context.textWidthImpl(text, textSize, fontName, len);
    }

    public abstract double textWidthImpl(String text, double textSize, String fontName, int len);

    public static int getCharIndex(String text, double textSize, String fonrName, double x) {
        return context.getCharIndexImpl(text, textSize, fonrName, x);
    }

    public abstract int getCharIndexImpl(String text, double textSize, String fontName, double x);

    public static boolean isDarkMode() {
        return context.isDarkModeImpl();
    }

    protected abstract boolean isDarkModeImpl();

    public static Vector4f getBaseColor() {
        return context.getBaseColorImpl();
    }

    protected abstract Vector4f getBaseColorImpl();

    public static double getUIScale() {
        return context.uiScale;
    }

    public static String getDefaultFontName() {
        return context.defaultFontName;
    }

    public static UIElement getSelectedElement() {
        return context.selectedElement;
    }

    public static int getModifiers() {
        return context.modifiers;
    }

    public static boolean isLeftMousePressed() {
        return context.leftMousePressed;
    }

    public static boolean isRightMousePressed() {
        return context.rightMousePressed;
    }

    public void setRoot(UIRoot root) {
        this.root = root;
    }

    public static UIRoot getRoot() {
        return context.root;
    }

    public void setRootSize(int width, int height) {
        rootSize.set(width, height);
        root.setFixedSize(rootSize);
    }

    public static SVector getRootSize() {
        return context.rootSize;
    }

    public void setUIScale(double uiScale) {
        this.uiScale = uiScale;
    }

    public static void setContext(UI context) {
        UI.context = context;
    }

    public static UI getContext() {
        return context;
    }

    // private static class DialogLock {
    // int returnCode;

    // DialogLock() {
    // }
    // }
}