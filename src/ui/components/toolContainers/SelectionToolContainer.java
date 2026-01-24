package ui.components.toolContainers;

import main.apps.MainApp;
import main.tools.ImageTool;
import main.tools.SelectionTool;
import sutil.math.SVector;
import sutil.ui.UIImage;

public final class SelectionToolContainer extends DragToolContainer<SelectionTool> {

    public SelectionToolContainer(MainApp app) {
        super(ImageTool.SELECTION, app);

        zeroMargin();

        add(new SelectionImage());
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