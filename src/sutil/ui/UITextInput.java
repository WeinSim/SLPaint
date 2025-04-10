package sutil.ui;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.lwjgl.glfw.GLFW;

public class UITextInput extends UIContainer {

    private boolean numberInput;

    protected UIText uiText;

    private Cursor cursor;

    private Consumer<String> valueUpdater;

    public UITextInput(Supplier<String> textUpdater, Consumer<String> valueUpdater) {
        super(HORIZONTAL, LEFT, CENTER);
        this.valueUpdater = valueUpdater;

        hMarginScale = 0.5;
        vMarginScale = 0.5;
        paddingScale = 0.33;

        uiText = new UIText(textUpdater);
        add(uiText);

        outlineNormal = true;
        cursor = new Cursor();
        add(cursor);

        selectable = true;
        selectOnClick = true;

        setLeftClickAction(cursor::resetTimer);
    }

    @Override
    public void update() {
        super.update();
        setHFixedSize(uiText.getTextSize() * 3.3333);
    }

    @Override
    public void keyPressed(int key) {
        if (active()) {
            String text = uiText.getText();
            String newText = text;
            if (key == GLFW.GLFW_KEY_BACKSPACE) {
                int len = text.length();
                if (len > 0) {
                    newText = text.substring(0, len - 1);
                }
            } else if (key == GLFW.GLFW_KEY_ENTER) {
                panel.setSelectedElement(null);
            } else {
                return;
            }

            valueUpdater.accept(newText);
            cursor.resetTimer();
        }
    }

    public void charInput(char c) {
        if (active()) {
            String text = uiText.getText();
            String newText = text;
            boolean validKey = false;
            if (numberInput) {
                validKey = c >= '0' && c <= '9';
            } else {
                if (c >= 32 && c <= 126)
                    validKey = true;
                if (c >= 160 && c < 255)
                    validKey = true;
            }
            if (!validKey)
                return;

            newText = text + c;
            valueUpdater.accept(newText);
            cursor.resetTimer();
        }
    }

    private boolean active() {
        return panel.getSelectedElement() == this;
    }

    public String getText() {
        return uiText.getText();
    }

    public void setNumberInput(boolean numberInput) {
        this.numberInput = numberInput;
    }

    private class Cursor extends UIContainer {

        private static final double BLINK_INTERVAL = 0.6;

        private double blinkStart;

        public Cursor() {
            super(VERTICAL, 0);

            zeroMargin();
            setVFillSize();
        }

        public void resetTimer() {
            blinkStart = System.nanoTime() * 1e-9;
        }

        @Override
        public void update() {
            super.update();

            outlineNormal = active() && ((System.nanoTime() * 1e-9 - blinkStart) / BLINK_INTERVAL) % 2 < 1;
        }
    }
}