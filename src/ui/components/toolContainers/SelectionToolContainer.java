package ui.components.toolContainers;

import main.apps.MainApp;
import main.tools.ImageTool;
import main.tools.SelectionTool;
import sutil.math.SVector;
import sutil.ui.UIImage;
import ui.components.SizeKnob;

public final class SelectionToolContainer extends DragToolContainer<SelectionTool> {

    private SizeKnob dragKnob;
    private SVector dragStartPos;
    private SVector dragStartSize;
    private SVector dragStartMouse;

    public SelectionToolContainer(MainApp app) {
        super(ImageTool.SELECTION, app);

        zeroMargin();

        add(new SelectionImage());

        dragKnob = null;
        dragStartPos = new SVector();
        dragStartSize = new SVector();
        dragStartMouse = new SVector();
    }

    @Override
    public void update() {
        super.update();

        // resizing
        if (!panel.isLeftMousePressed()) {
            dragKnob = null;
        }
        if (dragKnob != null) {
            SVector mouseDelta = app.getMouseImagePosVec().sub(dragStartMouse);
            dragKnob.updateSelection(dragStartPos, dragStartSize, mouseDelta);
        }
    }

    @Override
    protected boolean canStartIdleDrag() {
        return true;
    }

    public void startSizeDrag(SizeKnob knob) {
        dragKnob = knob;
        dragStartPos.set(ImageTool.SELECTION.getX(), ImageTool.SELECTION.getY());
        dragStartSize.set(ImageTool.SELECTION.getWidth(), ImageTool.SELECTION.getHeight());
        dragStartMouse.set(app.getMouseImagePosVec());
    }

    private class SelectionImage extends UIImage {

        SelectionImage() {
            super(0, new SVector());

            setVisibilitySupplier(() -> ImageTool.SELECTION.getSelection() != null);

            // the selection border should render above the image
            relativeLayer = -1;
        }

        @Override
        public void update() {
            super.update();

            setTextureID(ImageTool.SELECTION.getSelection().getTextureID());
        }

        @Override
        public void setPreferredSize() {
            int width = ImageTool.SELECTION.getWidth(),
                    height = ImageTool.SELECTION.getHeight();

            // size.set(Math.abs(width), Math.abs(height));
            size.set(width, height);
            size.scale(app.getImageZoom());
        }
    }
}