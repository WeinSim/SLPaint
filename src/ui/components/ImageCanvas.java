package ui.components;

import static org.lwjgl.glfw.GLFW.*;

import main.apps.MainApp;
import main.image.Image;
import main.tools.FillBucketTool;
import main.tools.ImageTool;
import main.tools.LineTool;
import main.tools.PencilTool;
import main.tools.PipetteTool;
import main.tools.Resizable;
import main.tools.SelectionTool;
import main.tools.TextTool;
import sutil.math.SVector;
import sutil.ui.UI;
import sutil.ui.UIColors;
import sutil.ui.UISizes;
import sutil.ui.elements.UIContainer;
import sutil.ui.elements.UIFloatContainer;
import sutil.ui.elements.UIImage;
import ui.components.toolContainers.FillBucketToolContainer;
import ui.components.toolContainers.LineToolContainer;
import ui.components.toolContainers.PencilToolContainer;
import ui.components.toolContainers.PipetteToolContainer;
import ui.components.toolContainers.SelectionToolContainer;
import ui.components.toolContainers.TextToolContainer;

public class ImageCanvas extends UIContainer {

    private static final int MIN_ZOOM_LEVEL = -4;
    private static final int MAX_ZOOM_LEVEL = 8;
    private static final double ZOOM_BASE = 1.6;

    private MainApp app;

    private SVector imageTranslation;
    private int imageZoomLevel;
    private boolean draggingImage;

    private int newX, newY, newWidth, newHeight;
    private boolean resizing;

    public ImageCanvas(int orientation, int hAlignment, int vAlignment, MainApp app) {
        super(orientation, hAlignment, vAlignment);

        this.app = app;
        app.setCanvas(this);

        noOutline();
        setFillSize();
        zeroMargin();

        setLeftClickAction(this::leftClick);
        setRightClickAction(this::rightClick);

        setCursorShape(() -> draggingImage ? GLFW_POINTING_HAND_CURSOR : null);

        style.setBackgroundColor(UIColors.CANVAS);

        clipChildren = true;

        add(new ImageResize());
        add(new ImageDisplay());

        for (ImageTool tool: ImageTool.INSTANCES) {
            add(switch(tool) {
                case PencilTool _ -> new PencilToolContainer(app);
                case LineTool _ -> new LineToolContainer(app);
                case PipetteTool _ -> new PipetteToolContainer(app);
                case FillBucketTool _ -> new FillBucketToolContainer(app);
                case TextTool _ -> new TextToolContainer(app);
                case SelectionTool _ -> new SelectionToolContainer(app);
                default -> throw new RuntimeException(
                    String.format("No ToolContainer found for tool \"%s\"", tool.getName()));
            });
        }

        resetImageTransform();

        draggingImage = false;
        resizing = false;
    }

    @Override
    public void update() {
        super.update();

        // stop dragging image
        if (draggingImage) {
            int mods = UI.getModifiers();
            boolean control = (mods & GLFW_MOD_CONTROL) != 0;
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

    private void leftClick() {
        int mods = UI.getModifiers();
        toolClick(GLFW_MOUSE_BUTTON_LEFT, mods);
    }

    private void rightClick() {
        int mods = UI.getModifiers();
        toolClick(GLFW_MOUSE_BUTTON_RIGHT, mods);

        if (canDoScrollZoom()) {
            // dragging image
            if ((mods & GLFW_MOD_CONTROL) != 0) {
                draggingImage = true;
            }
        }
    }

    private void toolClick(int mouseButton, int mods) {
        if ((mods & (GLFW_MOD_CONTROL | GLFW_MOD_SHIFT)) == 0) {
            int[] mousePosition = app.getMouseImagePosition();
            int mouseX = mousePosition[0],
                    mouseY = mousePosition[1];

            app.getActiveTool().click(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    public boolean mouseWheel(SVector scroll, int mods) {
        if (super.mouseWheel(scroll, mods))
            return true;

        if (canDoScrollZoom()) {
            if ((mods & GLFW_MOD_CONTROL) != 0) {
                // zoom
                zoom((int) Math.signum(scroll.y), new SVector(mousePosition).sub(position));
            } else {
                // scroll
                if ((mods & GLFW_MOD_SHIFT) != 0) {
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

    private void zoom(int delta, SVector origin) {
        double prevZoom = getImageZoom();
        imageZoomLevel += delta;
        imageZoomLevel = Math.min(Math.max(MIN_ZOOM_LEVEL, imageZoomLevel), MAX_ZOOM_LEVEL);
        double zoom = getImageZoom();
        imageTranslation.sub(origin).scale(zoom / prevZoom).add(origin);
    }

    public boolean canDoScrollZoom() {
        return calculateMouseAbove(mousePosition);
    }

    public void resetImageTransform() {
        imageTranslation = new SVector(10, 10).scale(UI.getUIScale());
        imageZoomLevel = 0;
    }

    public void zoomIn() {
        zoom(1, new SVector(size).div(2));
    }

    public boolean canZoomIn() {
        return imageZoomLevel < MAX_ZOOM_LEVEL;
    }

    public void zoomOut() {
        zoom(-1, new SVector(size).div(2));
    }

    public boolean canZoomOut() {
        return imageZoomLevel > MIN_ZOOM_LEVEL;
    }

    public void resetZoom() {
        zoom(-imageZoomLevel, new SVector(size).div(2));
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

    public void translateImage(SVector delta) {
        imageTranslation.add(delta.scale(getImageZoom()));
    }

    public boolean isImageResizing() {
        return resizing;
    }

    public int getNewImageWidth() {
        return newWidth;
    }

    public int getNewImageHeight() {
        return newHeight;
    }

    private class ImageDisplay extends UIFloatContainer {

        ImageDisplay() {
            super(0, 0);

            noOutline();
            noBackground();
            zeroMargin();

            addAnchor(Anchor.TOP_LEFT, ImageCanvas.this::getImageTranslation);

            add(new ImageContainerChild());
        }

        // Horrible name but whatever.
        // This needs to be its own class because it is a UIImage (and ImageContainer is
        // a subclass of UIFloatContainer).
        private class ImageContainerChild extends UIImage {

            ImageContainerChild() {
                super(() -> app.getImage().getTextureID(), new SVector());

                style.setBackgroundCheckerboard(UIColors.TRANSPARENCY_1, UIColors.TRANSPARENCY_2, UISizes.CHECKERBOARD);
            }

            @Override
            public void setPreferredSize() {
                Image image = app.getImage();

                size.set(image.getWidth(), image.getHeight());
                size.scale(app.getImageZoom());
            }
        }
    }

    private class ImageResize extends UIFloatContainer implements Resizable {

        ImageResize() {
            super(0, 0);

            noOutline();
            noBackground();
            zeroMargin();

            style.setStrokeCheckerboard(
                    () -> resizing,
                    UIColors.SELECTION_BORDER_1,
                    UIColors.SELECTION_BORDER_2,
                    () -> UISizes.CHECKERBOARD.get());
            style.setStrokeWeight(() -> 2 * UISizes.STROKE_WEIGHT.get());

            addAnchor(Anchor.TOP_LEFT, this::getPos);

            for (int dy = 0; dy <= 2; dy++) {
                for (int dx = 0; dx <= 2; dx++) {
                    if (dx == 1 && dy == 1)
                        continue;

                    add(new SizeKnob(this, dx, dy, () -> true, app));
                }
            }
        }

        @Override
        public void update() {
            newX = 0;
            newY = 0;
            newWidth = app.getImage().getWidth();
            newHeight = app.getImage().getHeight();

            super.update();

            setFixedSize(new SVector(newWidth, newHeight).scale(getImageZoom()));
        }

        private SVector getPos() {
            SVector pos = getImageTranslation().copy();
            double zoom = getImageZoom();
            pos.x += newX * zoom;
            pos.y += newY * zoom;
            return pos;
        }

        @Override
        public void startDragging() {
            resizing = true;
        }

        @Override
        public void finishDragging() {
            app.cropImage(newX, newY, newWidth, newHeight);
            resizing = false;
        }

        @Override
        public boolean lockRatio() {
            return false;
        }

        @Override
        public int getX() {
            return newX;
        }

        @Override
        public void setX(int x) {
            newX = x;
        }

        @Override
        public int getY() {
            return newY;
        }

        @Override
        public void setY(int y) {
            newY = y;
        }

        @Override
        public int getWidth() {
            return newWidth;
        }

        @Override
        public void setWidth(int width) {
            newWidth = Math.min(Math.max(MainApp.MIN_IMAGE_SIZE, width), MainApp.MAX_IMAGE_SIZE);
        }

        @Override
        public int getHeight() {
            return newHeight;
        }

        @Override
        public void setHeight(int height) {
            newHeight = Math.min(Math.max(MainApp.MIN_IMAGE_SIZE, height), MainApp.MAX_IMAGE_SIZE);
        }
    }
}