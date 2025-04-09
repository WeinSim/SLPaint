package renderEngine;

import main.Image;
import main.apps.MainApp;
import main.tools.ImageTool;
import main.tools.SelectionTool;
import main.tools.XYWH;
import sutil.math.SVector;
import ui.Colors;

public class MainAppRenderer extends AppRenderer<MainApp> {

    private static final SVector[] SELECTION_BORDER_COLORS = new SVector[] { new SVector(), new SVector(1, 1, 1) };

    public MainAppRenderer(MainApp app) {
        super(app);
    }

    @Override
    public void render() {
        uiMaster.start();
        uiMaster.setBGColor(Colors.getCanvasColor());

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
        // test: draw text fbo on top of image
        // FrameBufferObject fbo = uiMaster.getTextFBO();
        // uiMaster.image(fbo.textureID(), new SVector(-fbo.width(), 0), new SVector(fbo.width(), fbo.height()));

        // render selection
        ImageTool activeTool = app.getActiveTool();
        if (activeTool == ImageTool.SELECTION) {
            SelectionTool selection = ImageTool.SELECTION;
            Image selectionImg = selection.getSelection();
            if (selectionImg != null) {
                SVector position = new SVector(selection.getX(), selection.getY());
                SVector size = new SVector(selection.getWidth(),
                        selection.getHeight());
                uiMaster.image(selectionImg.getTextureID(), position, size);
            }
        }

        if (activeTool == ImageTool.SELECTION || activeTool == ImageTool.TEXT) {
            XYWH selection = (XYWH) activeTool;
            uiMaster.resetMatrix();
            uiMaster.translate(translation);
            int selectionPhase = activeTool.getState();
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