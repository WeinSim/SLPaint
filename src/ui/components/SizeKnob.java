package ui.components;

import java.util.function.Supplier;

import org.lwjgl.glfw.GLFW;
import org.lwjglx.util.vector.Vector4f;

import main.apps.MainApp;
import main.tools.Resizable;
import sutil.math.SVector;
import sutil.ui.UI;
import sutil.ui.UIContainer;
import sutil.ui.UIFloatContainer;
import sutil.ui.UISizes;

public class SizeKnob extends UIFloatContainer {

    private final int dx, dy;

    private final Resizable resizable;

    private MainApp app;

    /**
     * Wether {@code this} SizeKnob is currently being dragged.
     */
    private boolean dragging;
    private SVector dragStartPos;
    private SVector dragStartSize;
    private SVector dragStartMouse;

    public SizeKnob(int dx, int dy, UIContainer parent, Resizable resizable, Supplier<Boolean> visibilitySupplier,
            MainApp app) {
        super(0, 0);
        this.dx = dx;
        this.dy = dy;
        this.resizable = resizable;
        this.app = app;

        style.setBackgroundColor(new Vector4f(1, 1, 1, 1));
        style.setStrokeColor(new Vector4f(0, 0, 0, 1));
        style.setStrokeWeight(() -> UISizes.STROKE_WEIGHT.get() * 2.0);

        setFixedSize(UISizes.SIZE_KNOB.getWidthHeight());

        setVisibilitySupplier(visibilitySupplier);
        setLeftClickAction(this::startDrag);
        setCursorShape(() -> {
            if (!mouseAbove && !dragging)
                return null;
            if (dx == 1)
                return GLFW.GLFW_RESIZE_NS_CURSOR;
            if (dy == 1)
                return GLFW.GLFW_RESIZE_EW_CURSOR;
            return GLFW.GLFW_RESIZE_ALL_CURSOR;
        });

        Anchor parentAnchor = Anchor.fromOffsets(dx, dy);
        addAnchor(Anchor.CENTER_CENTER, parent, parentAnchor);

        dragging = false;
        dragStartPos = new SVector();
        dragStartSize = new SVector();
        dragStartMouse = new SVector();
    }

    @Override
    public void update() {
        super.update();

        if (dragging) {
            SVector mouseDelta = app.getMouseImagePosVec().sub(dragStartMouse);
            updateSelection(dragStartPos, dragStartSize, mouseDelta, resizable.lockRatio());

            if (!UI.isLeftMousePressed()) {
                dragging = false;
                resizable.finishResizing();
            }
        }
    }

    private void startDrag() {
        dragging = true;
        dragStartPos.set(resizable.getX(), resizable.getY());
        dragStartSize.set(resizable.getWidth(), resizable.getHeight());
        dragStartMouse.set(app.getMouseImagePosVec());

        resizable.startResizing();
    }

    public void updateSelection(SVector dragStartPos, SVector dragStartSize, SVector mouseDelta, boolean lockRatio) {
        // Size

        double oldWidth = dragStartSize.x,
                oldHeight = dragStartSize.y;
        double newWidth = oldWidth,
                newHeight = oldHeight;

        newWidth += mouseDelta.x * (dx - 1);
        newHeight += mouseDelta.y * (dy - 1);

        if (lockRatio) {
            double ratio = oldWidth / oldHeight;
            if (dx == 1) {
                // change only height
                newWidth = newHeight * ratio;
            } else if (dy == 1) {
                // change only width
                newHeight = newWidth / ratio;
            } else {
                // change both

                // find projection of target size onto original size
                double scale = (newWidth * oldWidth + newHeight * oldHeight)
                        / (oldWidth * oldWidth + oldHeight * oldHeight);

                newWidth = scale * oldWidth;
                newHeight = scale * oldHeight;
            }
        }

        resizable.setWidth((int) Math.round(newWidth));
        resizable.setHeight((int) Math.round(newHeight));

        // Position

        double x = dragStartPos.x,
                y = dragStartPos.y;

        if (dx == 0)
            x -= resizable.getWidth() - oldWidth;
        if (dy == 0)
            y -= resizable.getHeight() - oldHeight;

        resizable.setX((int) Math.round(x));
        resizable.setY((int) Math.round(y));
    }

    public boolean isDragging() {
        return dragging;
    }
}