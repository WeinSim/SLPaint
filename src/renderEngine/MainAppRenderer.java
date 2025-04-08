package renderEngine;

import main.Image;
import main.apps.MainApp;
import main.tools.ImageTool;
import main.tools.SelectionTool;
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

        // render image
        Image image = app.getImage();
        int width = image.getWidth(), height = image.getHeight();
        uiMaster.strokeWeight(0);
        uiMaster.noStroke();
        uiMaster.checkerboardFill(Colors.getTransparentColors(), 10);
        SVector imageSize = new SVector(width, height);
        uiMaster.rect(new SVector(), imageSize);
        uiMaster.image(image.getTextureID(), new SVector(), imageSize);

        // render selection
        if (app.getActiveTool() == ImageTool.SELECTION) {
            SelectionTool selection = ImageTool.SELECTION;
            Image selectionImg = selection.getSelection();
            if (selectionImg != null) {
                SVector position = new SVector(selection.getX(), selection.getY());
                SVector size = new SVector(selection.getWidth(),
                        selection.getHeight());
                uiMaster.image(selectionImg.getTextureID(), position, size);
            }

            uiMaster.resetMatrix();
            uiMaster.translate(translation);
            int selectionPhase = selection.getState();
            if (selectionPhase != SelectionTool.NONE) {
                int x, y, w, h;
                if (selectionPhase == ImageTool.INITIAL_DRAG) {
                    int startX = selection.getStartX();
                    int startY = selection.getStartY();
                    int endX = selection.getEndX();
                    int endY = selection.getEndY();
                    x = Math.min(startX, endX);
                    y = Math.min(startY, endY);
                    w = Math.abs(endX - startX);
                    h = Math.abs(endY - startY);
                } else {
                    x = selection.getX();
                    y = selection.getY();
                    w = selection.getWidth();
                    h = selection.getHeight();
                }
                uiMaster.checkerboardStroke(SELECTION_BORDER_COLORS, 15);
                uiMaster.strokeWeight(2);
                uiMaster.noFill();
                uiMaster.rect(new SVector(x, y).scale(zoom), new SVector(w, h).scale(app.getImageZoom()));
            }
        }

        // render ui
        renderUI();

        uiMaster.stop();
    }
}