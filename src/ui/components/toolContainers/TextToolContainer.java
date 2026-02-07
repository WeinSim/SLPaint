package ui.components.toolContainers;

import main.apps.MainApp;
import main.tools.DragTool;
import main.tools.ImageTool;
import main.tools.TextTool;
import sutil.ui.elements.UITextInput;

public final class TextToolContainer extends DragToolContainer<TextTool> {

    private TextInput textInput;

    public TextToolContainer(MainApp app) {
        super(ImageTool.TEXT, app);

        textInput = new TextInput();
        add(textInput);
    }

    private boolean showChildren() {
        int state = tool.getState();
        final int visibleStates = DragTool.IDLE | DragTool.IDLE_DRAG;
        return (state & visibleStates) != 0;
    }

    private class TextInput extends UITextInput {

        TextInput() {
            super(ImageTool.TEXT::getText, ImageTool.TEXT::setText);

            zeroMargin();
            setFillSize();
            noOutline();

            hAlignment = LEFT;
            vAlignment = TOP;

            relativeLayer = 1;

            setVisibilitySupplier(TextToolContainer.this::showChildren);

            uiText.setTextSize(() -> tool.getSize() * app.getImageZoom());
            uiText.setFontName(tool::getFont);
            uiText.setColor(() -> MainApp.toVector4f(app.getPrimaryColor()));

            app.setTextToolInput(this);
        }
    }
}