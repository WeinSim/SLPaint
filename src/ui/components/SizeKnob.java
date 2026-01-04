package ui.components;

import org.lwjglx.util.vector.Vector4f;

import main.tools.DragTool;
import main.tools.ImageTool;
import sutil.math.SVector;
import sutil.ui.UIFloatContainer;
import ui.Sizes;
import ui.components.toolContainers.SelectionToolContainer;

public class SizeKnob extends UIFloatContainer {

    private final int dx, dy;

    public SizeKnob(SelectionToolContainer parent, int dx, int dy) {
        super(0, 0);
        this.dx = dx;
        this.dy = dy;

        style.setBackgroundColor(new Vector4f(1, 1, 1, 1));
        style.setStrokeColor(new Vector4f(0, 0, 0, 1));
        style.setStrokeWeight(2.0);

        clipToRoot = false;
        relativeLayer = 3;

        double width = Sizes.SIZE_KNOB.width,
                height = Sizes.SIZE_KNOB.height;
        setFixedSize(new SVector(width, height));

        setLeftClickAction(() -> parent.startSizeDrag(this));
        setVisibilitySupplier(() -> (ImageTool.SELECTION.getState() & (DragTool.IDLE | DragTool.IDLE_DRAG)) != 0);

        Anchor parentAttachPoint = Anchor.fromOffsets(dx, dy);
        addAnchor(Anchor.CENTER_CENTER, parent, parentAttachPoint);
    }

    public void updateSelection(SVector dragStartPos, SVector dragStartSize, SVector mouseDelta) {
        // update x
        if (dx == 0) {
            double oldX = dragStartPos.x;
            double newX = oldX + mouseDelta.x;
            ImageTool.SELECTION.setX((int) Math.round(newX));
        }
        // update width
        if (dx != 1) {
            double oldWidth = dragStartSize.x;
            double newWidth = oldWidth + mouseDelta.x * (dx - 1);
            ImageTool.SELECTION.setWidth((int) Math.round(newWidth));
        }

        // update y
        if (dy == 0) {
            double oldY = dragStartPos.y;
            double newY = oldY + mouseDelta.y;
            ImageTool.SELECTION.setY((int) Math.round(newY));
        }
        // update width
        if (dy != 1) {
            double oldHeight = dragStartSize.y;
            double newHeight = oldHeight + mouseDelta.y * (dy - 1);
            ImageTool.SELECTION.setHeight((int) Math.round(newHeight));
        }
    }
}