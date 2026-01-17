package ui.components;

import org.lwjgl.glfw.GLFW;

import main.Image;
import main.apps.MainApp;
import main.tools.ImageTool;
import main.tools.ImageTool.KeyboardShortcut;
import sutil.math.SVector;
import sutil.ui.UIColors;
import sutil.ui.UIContainer;
import sutil.ui.UIElement;
import sutil.ui.UIFloatContainer;
import sutil.ui.UIImage;
import sutil.ui.UI;
import sutil.ui.UISizes;
import ui.components.toolContainers.FillBucketToolContainer;
import ui.components.toolContainers.PencilToolContainer;
import ui.components.toolContainers.PipetteToolContainer;
import ui.components.toolContainers.SelectionToolContainer;
import ui.components.toolContainers.TextToolContainer;
import ui.components.toolContainers.ToolContainer;

public class ImageCanvas extends UIContainer {

    /**
     * The number of layers that a {@code ImageCanvas} needs.
     * <p>
     * 
     * 0 = image
     * 1 = selection image
     * 2 = selection border, text input
     * 3 = size knobs
     */
    public static final int NUM_UI_LAYERS = 4;

    private static final int MIN_ZOOM_LEVEL = -4;
    private static final int MAX_ZOOM_LEVEL = 8;
    private static final double ZOOM_BASE = 1.6;

    private MainApp app;

    private SVector imageTranslation;
    private int imageZoomLevel;
    private boolean draggingImage;

    public ImageCanvas(int orientation, int hAlignment, int vAlignment, MainApp app) {
        super(orientation, hAlignment, vAlignment);

        this.app = app;
        app.setCanvas(this);

        noOutline();
        setFillSize();
        zeroMargin();

        style.setBackgroundColor(UIColors.CANVAS);

        // TODO: calling resetImageTransform here crashes because the ImageCanvas
        // doesn't know its panel yet.
        // resetImageTransform();
        imageTranslation = new SVector();

        draggingImage = false;

        add(new ImageContainer());

        add(new PencilToolContainer(app));
        add(new PipetteToolContainer(app));
        add(new FillBucketToolContainer(app));
        add(new TextToolContainer(app));
        add(new SelectionToolContainer(app));
    }

    @Override
    public void update() {
        super.update();

        // stop dragging image
        // TODO: this is kind of ugly that I have to get keyboard input (and mouse
        // position too, see below) from the app directly rather than through the UI
        if (draggingImage) {
            int mods = app.getModifierKeys();
            boolean control = (mods & GLFW.GLFW_MOD_CONTROL) != 0;
            if (!UI.isRightMousePressed() || !control) {
                draggingImage = false;
            }
        }

        // dragging image
        if (draggingImage) {
            SVector mouseMovement = new SVector(app.getMousePosition()).sub(app.getPrevMousePosition());
            imageTranslation.add(mouseMovement);
        }
    }

    @Override
    public void keyPressed(int key, int mods) {
        super.keyPressed(key, mods);

        // tools
        ImageTool shortcutTool = null;
        for (ImageTool tool : ImageTool.INSTANCES) {
            for (KeyboardShortcut shortcut : tool.getKeyboardShortcuts()) {
                if (shortcut.key() == key
                        && (shortcut.initialState() & tool.getState()) != 0
                        && shortcut.modifiers() == mods) {

                    if (shortcutTool != null) {
                        System.err.format(
                                "Found conflicting image tool keyboard shortcuts! tool 1 = %s, tool 2 = %s, key = %d, mods = %d\n",
                                shortcutTool, tool, key, mods);
                    }

                    app.setActiveTool(tool);
                    shortcut.action().run();
                    shortcutTool = tool;
                }
            }
        }
    }

    @Override
    public void mousePressed(int mouseButton, int mods) {
        super.mousePressed(mouseButton, mods);

        // tool click
        if (mouseAbove()) {
            if ((mods & (GLFW.GLFW_MOD_CONTROL | GLFW.GLFW_MOD_SHIFT)) == 0) {
                int[] mousePosition = app.getMouseImagePosition();
                int mouseX = mousePosition[0],
                        mouseY = mousePosition[1];

                app.getActiveTool().click(mouseX, mouseY, mouseButton);
            }
        }

        if (canDoScrollZoom()) {
            // dragging image
            if (mouseButton == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                if ((mods & GLFW.GLFW_MOD_CONTROL) != 0) {
                    draggingImage = true;
                }
            }
        }
    }

    @Override
    public boolean mouseWheel(SVector scroll, SVector mousePos, int mods) {
        if (super.mouseWheel(scroll, mousePos, mods))
            return true;

        if (canDoScrollZoom()) {
            if ((mods & GLFW.GLFW_MOD_CONTROL) != 0) {
                // zoom
                double prevZoom = getImageZoom();
                imageZoomLevel += (int) Math.signum(scroll.y);
                imageZoomLevel = Math.min(Math.max(MIN_ZOOM_LEVEL, imageZoomLevel), MAX_ZOOM_LEVEL);
                double zoom = getImageZoom();
                mousePos = new SVector(mousePos).sub(position);
                imageTranslation.sub(mousePos).scale(zoom / prevZoom).add(mousePos);
                app.getWindow().setHandCursor();
            } else {
                // scroll
                if ((mods & GLFW.GLFW_MOD_SHIFT) != 0) {
                    double temp = scroll.x;
                    scroll.x = scroll.y;
                    scroll.y = temp;
                }
                imageTranslation.add(scroll);
            }
            return true;
        }
        return false;
    }

    public boolean canDoScrollZoom() {
        if (mouseAbove())
            return true;

        for (UIElement child : getChildren()) {
            if (child instanceof ToolContainer) {
                if (child.mouseAbove())
                    return true;
            }
        }

        return false;
    }

    public void resetImageTransform() {
        imageTranslation = new SVector(10, 10).scale(UI.getUIScale());
        imageZoomLevel = 0;
    }

    public double getImageZoom() {
        return Math.pow(ZOOM_BASE, imageZoomLevel) * UI.getUIScale();
    }

    public SVector getImageTranslation() {
        return imageTranslation;
    }

    public SVector getImagePosition(SVector screenSpacePos) {
        return screenSpacePos.copy().sub(getAbsolutePosition()).sub(imageTranslation).div(getImageZoom());
    }

    public SVector getScreenPosition(SVector imagePos) {
        return imagePos.copy().scale(getImageZoom()).add(getAbsolutePosition().add(imageTranslation));
    }

    private class ImageContainer extends UIFloatContainer {

        public ImageContainer() {
            super(0, 0);

            noOutline();
            noBackground();
            zeroMargin();

            relativeLayer = 0;
            clipToRoot = false;

            add(new ImageContainerChild());
        }

        @Override
        public void update() {
            super.update();

            clearAnchors();
            addAnchor(Anchor.TOP_LEFT, imageTranslation);
        }

        // horrible name but whatever
        private class ImageContainerChild extends UIImage {

            public ImageContainerChild() {
                super(0, new SVector());

                style.setBackgroundCheckerboard(UIColors.TRANSPARENCY_1, UIColors.TRANSPARENCY_2, UISizes.CHECKERBOARD);
            }

            @Override
            public void update() {
                super.update();

                setTextureID(app.getImage().getTextureID());
            }

            @Override
            public void setPreferredSize() {
                Image image = app.getImage();

                size.set(image.getWidth(), image.getHeight());
                size.scale(app.getImageZoom());
            }
        }
    }
}