package ui.components.toolContainers;

import java.awt.image.BufferedImage;
import java.util.function.BooleanSupplier;

import org.lwjgl.glfw.GLFW;

import main.Image;
import main.apps.MainApp;
import main.tools.Draggable;
import main.tools.ImageTool;
import main.tools.LineTool;
import sutil.math.SVector;
import sutil.ui.elements.UIImage;
import ui.components.DragKnob;

public final class LineToolContainer extends ToolContainer<LineTool> {

    private Image previewImage;

    public LineToolContainer(MainApp app) {
        super(ImageTool.LINE, app);

        addAnchor(Anchor.TOP_LEFT, () -> app.getCanvas().getImageTranslation());

        zeroMargin();
        relativeLayer = 2;

        add(new ImageDisplay());

        final int visibleStates = LineTool.IDLE;
        BooleanSupplier dragKnobVis = () -> (tool.getState() & visibleStates) != 0;
        int cursorShape = GLFW.GLFW_RESIZE_ALL_CURSOR;
        Draggable[] draggables = { ImageTool.LINE.getDrag1(), ImageTool.LINE.getDrag2() };
        // Anchor[] anchors = { Anchor.TOP_LEFT, Anchor.BOTTOM_RIGHT };
        for (int i = 0; i < 2; i++) {
            final Draggable d = draggables[i];
            add(new DragKnob(d, dragKnobVis, cursorShape, () -> getDragKnobPos(d), app));
            // add(new DragKnob(d, dragKnobVis, cursorShape, anchors[i], app));
        }

        Image image = app.getImage();
        int width = image.getWidth(),
                height = image.getHeight();
        previewImage = new Image(new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB));
    }

    @Override
    public void update() {
        super.update();

        Image image = app.getImage();
        int width = image.getWidth(),
                height = image.getHeight();
        int[] pixels = new int[width * height];
        previewImage.resize(width, height, pixels);

        switch (tool.getState()) {
            case LineTool.INITIAL_DRAG -> {
                int[] imageMousePos = app.getMouseImagePosition();
                tool.getDrag2().setX(imageMousePos[0]);
                tool.getDrag2().setY(imageMousePos[1]);

                drawPreviewLine();
            }
            case LineTool.IDLE -> {
                drawPreviewLine();
            }
        }

        previewImage.updateOpenGLTexture();
    }

    private void drawPreviewLine() {
        Draggable d1 = tool.getDrag1(),
                d2 = tool.getDrag2();
        previewImage.drawLine(d1.getX(), d1.getY(),
                d2.getX(), d2.getY(),
                tool.getSize(),
                app.getPrimaryColor(),
                true);
    }

    @Override
    public void mouseReleased(int mouseButton, int mods) {
        super.mouseReleased(mouseButton, mods);

        switch (tool.getState()) {
            case LineTool.INITIAL_DRAG -> tool.setState(LineTool.IDLE);
        }
    }

    @Override
    protected int getVisibleStates() {
        return LineTool.INITIAL_DRAG | LineTool.IDLE;
    }

    @Override
    protected boolean calculateMouseAbove(SVector mouse) {
        return false;
    }

    private SVector getDragKnobPos(Draggable draggable) {
        SVector p = new SVector(draggable.getX(), draggable.getY());
        p.scale(app.getImageZoom());
        return p;
    }

    // copied from ImageCanvas
    private class ImageDisplay extends UIImage {

        ImageDisplay() {
            super(() -> previewImage.getTextureID(), new SVector());
        }

        @Override
        public void setPreferredSize() {
            Image image = app.getImage();

            size.set(image.getWidth(), image.getHeight());
            size.scale(app.getImageZoom());
        }

        @Override
        protected boolean calculateMouseAbove(SVector mouse) {
            return false;
        }
    }
}