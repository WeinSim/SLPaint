package sutil.ui.elements;

import static org.lwjgl.glfw.GLFW.*;

import java.util.function.Consumer;
import java.util.function.Supplier;

import sutil.math.SVector;
import sutil.ui.UI;
import sutil.ui.UISizes;

public class UITextInput extends UIContainer {

    private static final double BLINK_INTERVAL = 0.6;

    protected UIText uiText;

    private Supplier<String> textUpdater;
    private Consumer<String> valueUpdater;

    private boolean multiline;
    private int cursorPosition;

    private double blinkStart;

    public UITextInput(Supplier<String> textUpdater, Consumer<String> valueUpdater) {
        this(textUpdater, valueUpdater, false);
    }

    public UITextInput(Supplier<String> textUpdater, Consumer<String> valueUpdater, boolean multiline) {
        super(HORIZONTAL, LEFT, CENTER);

        this.textUpdater = textUpdater;
        this.valueUpdater = valueUpdater;
        this.multiline = multiline;

        hMarginScale = 0.5;
        vMarginScale = 0.5;
        outlineNormal = true;

        setHFillSize();

        setCursorShape(() -> mouseAbove ? GLFW_IBEAM_CURSOR : null);

        selectable = true;
        selectOnClick = true;

        uiText = new UIText(textUpdater);
        add(uiText);
        add(new Cursor());

        setLeftClickAction(this::click);

        cursorPosition = 0;
    }

    @Override
    public void keyPressed(int key, int mods) {
        super.keyPressed(key, mods);

        if (isSelected()) {
            String text = textUpdater.get();
            boundCursorPosition(text);
            switch (key) {
                case GLFW_KEY_BACKSPACE -> {
                    if (cursorPosition > 0) {
                        String newText = text.substring(0, cursorPosition - 1) + text.substring(cursorPosition);
                        cursorPosition--;

                        valueUpdater.accept(newText);
                        resetTimer();
                    }
                }
                case GLFW_KEY_DELETE -> {
                    if (cursorPosition < text.length()) {
                        String newText = text.substring(0, cursorPosition) + text.substring(cursorPosition + 1);

                        valueUpdater.accept(newText);
                        resetTimer();
                    }
                }
                case GLFW_KEY_LEFT -> {
                    cursorPosition--;
                    resetTimer();
                }
                case GLFW_KEY_RIGHT -> {
                    cursorPosition++;
                    resetTimer();
                }
                case GLFW_KEY_ENTER -> {
                    if (multiline)
                        charInput('\n');
                    else
                        UI.select(null);
                }
            }
        }
    }

    @Override
    public void charInput(char c) {
        if (isSelected()) {
            String text = textUpdater.get();
            boundCursorPosition(text);

            String newText = text;
            boolean validKey = isValidChar(c);
            if (!validKey)
                return;

            newText = text.substring(0, cursorPosition) + c + text.substring(cursorPosition);
            cursorPosition++;
            valueUpdater.accept(newText);
            resetTimer();
        }
    }

    protected boolean isValidChar(char c) {
        return (c >= 32 && c <= 126) || (c >= 160 && c < 255) || (c == '\n' && multiline);
    }

    private void boundCursorPosition(String text) {
        cursorPosition = Math.min(Math.max(cursorPosition, 0), text.length());
    }

    private void click() {
        cursorPosition = uiText.getCharIndex(mousePosition.x - position.x - uiText.getPosition().x);
        resetTimer();
    }

    public void resetTimer() {
        blinkStart = System.nanoTime() * 1e-9;
    }

    @Override
    public void select() {
        cursorPosition = textUpdater.get().length();
        resetTimer();
    }

    protected void setTextUpdater(Supplier<String> textUpdater) {
        this.textUpdater = textUpdater;
        uiText.setText(textUpdater);
    }

    protected void setValueUpdater(Consumer<String> valueUpdater) {
        this.valueUpdater = valueUpdater;
    }

    private class Cursor extends UIFloatContainer {

        public Cursor() {
            super(0, 0);

            addAnchor(Anchor.TOP_LEFT, () -> {
                SVector pos = new SVector(uiText.getPosition());
                pos.x += uiText.textWidth(cursorPosition);
                return pos;
            });

            setVisibilitySupplier(() -> UITextInput.this.isSelected()
                    && ((System.nanoTime() * 1e-9 - blinkStart) / BLINK_INTERVAL) % 2 < 1);
        }

        @Override
        public void update() {
            setFixedSize(new SVector(UISizes.STROKE_WEIGHT.get(), uiText.getTextSize()));
        }
    }
}