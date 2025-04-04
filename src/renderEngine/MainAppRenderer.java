package renderEngine;

import main.Image;
import main.SelectionManager;
import main.apps.MainApp;
import sutil.math.SVector;
import ui.Colors;

public class MainAppRenderer extends AppRenderer<MainApp> {

    private static final SVector[] SELECTION_BORDER_COLORS = new SVector[] { new SVector(), new SVector(1, 1, 1) };

    public MainAppRenderer(MainApp app) {
        super(app);
    }

    @Override
    public void render() {
        setBGColor(Colors.getCanvasColor());

        uiMaster.start();

        SVector translation = app.getImageTranslation();
        double zoom = app.getImageZoom();
        uiMaster.translate(translation);
        uiMaster.scale(zoom);

        Image image = app.getImage();
        int width = image.getWidth(), height = image.getHeight();
        uiMaster.strokeWeight(0);
        uiMaster.noStroke();
        uiMaster.checkerboardFill(Colors.getTransparentColors(), 10);
        SVector imageSize = new SVector(width, height);
        uiMaster.rect(new SVector(), imageSize);
        uiMaster.image(image.getTextureID(), new SVector(), imageSize);

        SelectionManager selectionManager = app.getSelectionManager();
        Image selection = selectionManager.getSelection();
        if (selection != null) {
            SVector position = new SVector(selectionManager.getX(), selectionManager.getY());
            SVector size = new SVector(selectionManager.getWidth(),
                    selectionManager.getHeight());
            uiMaster.image(selection.getTextureID(), position, size);
        }

        uiMaster.resetMatrix();
        uiMaster.translate(translation);
        int selectionPhase = selectionManager.getPhase();
        if (selectionPhase != SelectionManager.NONE) {
            int x, y, w, h;
            if (selectionPhase == SelectionManager.CREATING) {
                int startX = selectionManager.getStartX();
                int startY = selectionManager.getStartY();
                int endX = selectionManager.getEndX();
                int endY = selectionManager.getEndY();
                x = Math.min(startX, endX);
                y = Math.min(startY, endY);
                w = Math.abs(endX - startX);
                h = Math.abs(endY - startY);
            } else {
                x = selectionManager.getX();
                y = selectionManager.getY();
                w = selectionManager.getWidth();
                h = selectionManager.getHeight();
            }
            // TODO: remove these magic numbers
            uiMaster.checkerboardStroke(SELECTION_BORDER_COLORS, 15);
            uiMaster.strokeWeight(2);
            uiMaster.noFill();
            uiMaster.rect(new SVector(x, y).scale(zoom), new SVector(w, h).scale(app.getImageZoom()));
        }

        renderUI();

        uiMaster.stop();
    }
}