package renderEngine;

import main.Image;
import main.MainApp;
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

        Image selection = app.getSelection();
        if (selection != null) {
            SVector position = new SVector(app.getSelectionPosX(), app.getSelectionPosY());
            SVector size = new SVector(app.getSelectionWidth(),
                    app.getSelectionHeight());
            uiMaster.image(selection.getTextureID(), position, size);
        }

        uiMaster.resetMatrix();
        uiMaster.translate(translation);
        int selectionPhase = app.getSelectionPhase();
        if (selectionPhase != MainApp.NO_SELECTION) {
            int x, y, w, h;
            if (selectionPhase == MainApp.CREATING_SELECTION) {
                int startX = app.getSelectionStartX();
                int startY = app.getSelectionStartY();
                int endX = app.getSelectionEndX();
                int endY = app.getSelectionEndY();
                x = Math.min(startX, endX);
                y = Math.min(startY, endY);
                w = Math.abs(endX - startX);
                h = Math.abs(endY - startY);
            } else {
                x = app.getSelectionPosX();
                y = app.getSelectionPosY();
                w = app.getSelectionWidth();
                h = app.getSelectionHeight();
            }
            uiMaster.stroke(new SVector());
            uiMaster.strokeWeight(2);
            uiMaster.noFill();
            uiMaster.rect(new SVector(x, y).scale(zoom), new SVector(w, h).scale(app.getImageZoom()));
        }

        renderUI();

        uiMaster.stop();
    }
}