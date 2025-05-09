package ui.components;

import main.apps.MainApp;
import main.tools.ImageTool;
import sutil.math.SVector;
import sutil.ui.UIFloatContainer;
import sutil.ui.UITextInput;

public class TextFloatContainer extends UIFloatContainer {

    private MainApp app;

    private UITextInput textInput;

    public TextFloatContainer(MainApp app) {
        super(0, 0);

        this.app = app;

        zeroMargin();
        noBackground();
        noOutline();

        clipToRoot = false;

        setVisibilitySupplier(() -> app.getActiveTool() == ImageTool.TEXT
                && ImageTool.TEXT.getState() == ImageTool.IDLE);

        textInput = new TextInput();
        textInput.setFillSize();
        add(textInput);
    }

    @Override
    public void update() {
        super.update();

        if (ImageTool.TEXT.getState() == ImageTool.IDLE) {
            double zoom = app.getImageZoom();
            setFixedSize(new SVector(ImageTool.TEXT.getWidth(), ImageTool.TEXT.getHeight()).scale(zoom));
            clearAttachPoints();
            SVector position = new SVector(ImageTool.TEXT.getX(), ImageTool.TEXT.getY());
            position.scale(zoom);
            position.add(app.getImageTranslation());
            position.sub(app.getCanvas().getAbsolutePosition());
            addAttachPoint(UIFloatContainer.TOP_LEFT, position);
        }
    }

    private class TextInput extends UITextInput {

        TextInput() {
            super(ImageTool.TEXT::getText, ImageTool.TEXT::setText);

            zeroMargin();

            hAlignment = LEFT;
            vAlignment = TOP;

            style.setStrokeWeightSupplier(() -> 0.0);

            app.setTextToolInput(this);

            // ImageTool.TEXT::getSize doesn't work because it returns an int and not a
            // double (and Supplier<Integer> is not a subtype of Supplier<Double>)
            uiText.setTextSize(() -> ImageTool.TEXT.getSize() * app.getImageZoom());
            uiText.setFontName(() -> ImageTool.TEXT.getFont());
            uiText.setColor(() -> MainApp.toSVector(app.getPrimaryColor()));
        }

        @Override
        public void update() {
            super.update();

            setFillSize();
        }
    }
}