package renderEngine;

import main.Image;
import main.SelectionManager;
import main.apps.MainApp;
import sutil.math.SVector;

public class MainAppRenderer extends AppRenderer<MainApp> {

    public MainAppRenderer(MainApp app) {
        super(app);
    }

    @Override
    public void render() {
        setDefaultBGColor();

        uiMaster.start();

        SVector translation = app.getImageTranslation();
        double zoom = app.getImageZoom();
        uiMaster.translate(translation);
        uiMaster.scale(zoom);
        Image image = app.getImage();
        uiMaster.image(image.getTextureID(), new SVector(), new SVector(image.getWidth(), image.getHeight()));

        SelectionManager selectionManager = app.getSelectionManager();
        Image selection = selectionManager.getSelection();
        if (selection != null) {
            SVector position = new SVector(selectionManager.getX(), selectionManager.getY());
            SVector size = new SVector(selectionManager.getWidth(),
                    selectionManager.getHeight());
            uiMaster.clipArea(translation.copy(), new SVector(image.getWidth(), image.getHeight()).scale(zoom));
            uiMaster.image(selection.getTextureID(), position, size);
            uiMaster.noClip();
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
            uiMaster.stroke(new SVector());
            uiMaster.strokeWeight(2);
            uiMaster.checkerboardStroke(new SVector(1, 1, 1), 15);
            uiMaster.noFill();
            uiMaster.rect(new SVector(x, y).scale(zoom), new SVector(w, h).scale(app.getImageZoom()));
            uiMaster.noCheckerboardStroke();
        }

        renderUI();

        uiMaster.stop();
    }
}