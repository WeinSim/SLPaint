package ui.components;

import main.apps.MainApp;
import main.tools.ImageTool;
import main.tools.TextTool;
import sutil.ui.UITextInput;

public class TextToolContainer extends ToolContainer<TextTool> {

    public TextToolContainer(MainApp app) {
        super(ImageTool.TEXT, app);

        add(new TextInput());
    }

    private class TextInput extends UITextInput {

        TextInput() {
            super(ImageTool.TEXT::getText, ImageTool.TEXT::setText);

            zeroMargin();
            noOutline();
            setFillSize();

            hAlignment = LEFT;
            vAlignment = TOP;

            // this::showChildren doesn't work, probably because it defined by the
            // surrounging class' superclass (ToolContainer), which is not a superclass of
            // TextInput
            setVisibilitySupplier(() -> showChildren());

            uiText.setTextSize(() -> tool.getSize() * app.getImageZoom());
            uiText.setFontName(tool::getFont);
            uiText.setColor(() -> MainApp.toSVector(app.getPrimaryColor()));

            app.setTextToolInput(this);
        }
    }
}