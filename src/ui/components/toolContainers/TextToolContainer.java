package ui.components.toolContainers;

import main.apps.MainApp;
import main.tools.DragTool;
import main.tools.ImageTool;
import main.tools.TextTool;
import sutil.ui.UITextInput;

public final class TextToolContainer extends DragToolContainer<TextTool> {

    private TextInput textInput;

    public TextToolContainer(MainApp app) {
        super(ImageTool.TEXT, app);

        textInput = new TextInput();
        add(textInput);
    }

    private boolean showChildren() {
        int state = tool.getState();
        return state == DragTool.IDLE || state == DragTool.IDLE_DRAG;
    }

    @Override
    protected boolean canStartIdleDrag() {
        return !textInput.mouseAbove();
    }

    private class TextInput extends UITextInput {

        TextInput() {
            super(ImageTool.TEXT::getText, ImageTool.TEXT::setText);

            zeroMargin();
            setFillSize();
            noOutline();

            hAlignment = LEFT;
            vAlignment = TOP;

            setVisibilitySupplier(TextToolContainer.this::showChildren);

            uiText.setTextSize(() -> tool.getSize() * app.getImageZoom());
            uiText.setFontName(tool::getFont);
            uiText.setColor(() -> MainApp.toVector4f(app.getPrimaryColor()));

            app.setTextToolInput(this);
        }
    }
}