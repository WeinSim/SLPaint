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

    private static final boolean DEBUG_RENDERING = false;

    public MainAppRenderer(MainApp app) {
        super(app);
    }

    @Override
    public void render() {
        uiMaster.start();

        if (DEBUG_RENDERING) {
            renderDebug();
        } else {
            uiMaster.setBGColor(Colors.getCanvasColor());

            // layer 0:
            // subdiv 0: checkerboard background
            // subdiv 1: image
            // subdiv 2: selection content
            // subdiv 3: selection border

            SVector translation = app.getImageTranslation();
            double zoom = app.getImageZoom();
            uiMaster.translate(translation);
            uiMaster.scale(zoom);

            // render image
            layer = 0;
            foregroundDraw = false;
            Image image = app.getImage();
            int width = image.getWidth(), height = image.getHeight();
            SVector imageSize = new SVector(width, height);
            uiMaster.noStroke();
            uiMaster.checkerboardFill(Colors.getTransparentColors(), 10);
            uiMaster.depth(getDepth(0));
            uiMaster.rect(new SVector(), imageSize);
            uiMaster.depth(getDepth(1));
            uiMaster.image(image.getTextureID(), new SVector(), imageSize);

            // calling render() here to ensure that following transparent draw calls render
            // correctly (e.g. selection, text tool)
            uiMaster.render();

            // render selection
            ImageTool activeTool = app.getActiveTool();
            if (activeTool == ImageTool.SELECTION) {
                SelectionTool selection = ImageTool.SELECTION;
                Image selectionImg = selection.getSelection();
                if (selectionImg != null) {
                    SVector position = new SVector(selection.getX(), selection.getY());
                    SVector size = new SVector(selection.getWidth(),
                            selection.getHeight());
                    uiMaster.depth(getDepth(2));
                    uiMaster.image(selectionImg.getTextureID(), position, size);
                }
            }

            if (activeTool == ImageTool.SELECTION || activeTool == ImageTool.TEXT) {
                XYWH selection = (XYWH) activeTool;
                uiMaster.resetMatrix();
                uiMaster.translate(translation);
                int selectionPhase = activeTool.getState();
                if (selectionPhase != ImageTool.NONE) {
                    int x, y, w, h;
                    if (selectionPhase == ImageTool.INITIAL_DRAG) {
                        int startX = selection.getStartX();
                        int startY = selection.getStartY();
                        int endX = selection.getEndX();
                        int endY = selection.getEndY();
                        x = Math.min(startX, endX);
                        y = Math.min(startY, endY);
                        w = Math.abs(endX - startX) + 1;
                        h = Math.abs(endY - startY) + 1;
                    } else {
                        x = selection.getX();
                        y = selection.getY();
                        w = selection.getWidth();
                        h = selection.getHeight();
                    }
                    uiMaster.checkerboardStroke(SELECTION_BORDER_COLORS, 10);
                    uiMaster.strokeWeight(2);
                    uiMaster.noFill();
                    uiMaster.depth(getDepth(3));
                    uiMaster.rect(new SVector(x, y).scale(zoom), new SVector(w, h).scale(app.getImageZoom()));
                }
            }

            layer = 10;

            renderUI();
        }

        uiMaster.render();
    }

    private void renderDebug() {
        uiMaster.setBGColor(new SVector(1, 1, 1));

        uiMaster.hueSatField(new SVector(300, 300), new SVector(200, 200), true, true);

        // SVector p1 = new SVector(100, 100),
        // p2 = new SVector(300, 700),
        // p3 = new SVector(1000, 800);
        // SVector s1 = new SVector(200, 100),
        // s2 = new SVector(150, 150),
        // s3 = new SVector(100, 200);
        // SVector c1 = new SVector(0.8, 0.2, 0.2),
        // c2 = new SVector(0.2, 0.8, 0.2),
        // c3 = new SVector(0.2, 0.2, 0.8);

        // uiMaster.noStroke();

        // uiMaster.fill(c1);
        // uiMaster.rect(p1, s1);

        // uiMaster.checkerboardFill(Colors.getTransparentColors(), 15);
        // uiMaster.depth(getDepth(0, false));
        // uiMaster.rect(p2, s2);

        // p2.x += 50;

        // uiMaster.fill(c2);
        // uiMaster.fillAlpha(0.5);
        // uiMaster.depth(getDepth(1, false));
        // uiMaster.rect(p2, s2);

        // uiMaster.fillAlpha(1.0);

        // uiMaster.fill(c3);
        // uiMaster.rect(p3, s3);
    }
}