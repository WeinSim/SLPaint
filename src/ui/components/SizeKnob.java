package ui.components;

import org.lwjglx.util.vector.Vector4f;

import main.tools.DragTool;
import sutil.math.SVector;
import sutil.ui.UIFloatContainer;
import sutil.ui.UISizes;
import ui.components.toolContainers.DragToolContainer;

public class SizeKnob extends UIFloatContainer {

    private final int dx, dy;

    private final DragTool tool;

    public SizeKnob(DragToolContainer<?> parent, DragTool tool, int dx, int dy) {
        super(0, 0);
        this.dx = dx;
        this.dy = dy;
        this.tool = tool;

        style.setBackgroundColor(new Vector4f(1, 1, 1, 1));
        style.setStrokeColor(new Vector4f(0, 0, 0, 1));
        style.setStrokeWeight(2.0);

        clipToRoot = false;
        relativeLayer = 1;

        setLeftClickAction(() -> parent.startSizeDrag(this));
        final int visibleStates = DragTool.IDLE | DragTool.IDLE_DRAG | DragTool.RESIZING;
        setVisibilitySupplier(() -> (tool.getState() & visibleStates) != 0);

        Anchor parentAnchor = Anchor.fromOffsets(dx, dy);
        addAnchor(Anchor.CENTER_CENTER, parent, parentAnchor);
    }

    @Override
    public void update() {
        super.update();

        double wh = UISizes.SIZE_KNOB.get();
        setFixedSize(new SVector(wh, wh));
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

        tool.setWidth((int) Math.round(newWidth));
        tool.setHeight((int) Math.round(newHeight));

        // Position

        double x = dragStartPos.x,
                y = dragStartPos.y;

        if (dx == 0)
            x -= tool.getWidth() - oldWidth;
        if (dy == 0)
            y -= tool.getHeight() - oldHeight;

        tool.setX((int) Math.round(x));
        tool.setY((int) Math.round(y));
    }
}