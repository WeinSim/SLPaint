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

    private Consumer<String> valueUpdater;

    private boolean multiline;
    private int cursorPosition;

    private double blinkStart;

    public UITextInput(Supplier<String> textUpdater, Consumer<String> valueUpdater) {
        this(textUpdater, valueUpdater, false);
    }

    public UITextInput(Supplier<String> textUpdater, Consumer<String> valueUpdater, boolean multiline) {
        super(HORIZONTAL, LEFT, CENTER);

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

        setLeftClickAction(this::resetTimer);

        cursorPosition = 0;
    }

    @Override
    public void keyPressed(int key, int mods) {
        if (active()) {
            String text = uiText.getText();
            boundCursorPosition(text);
            switch (key) {
                case GLFW_KEY_BACKSPACE -> {
                    if (cursorPosition > 0) {
                        String newText = text.substring(0, cursorPosition - 1)
                                + text.substring(cursorPosition);
                        cursorPosition--;

                        updateText(newText);
                        resetTimer();
                    }
                }
                case GLFW_KEY_DELETE -> {
                    if (cursorPosition < text.length()) {
                        String newText = text.substring(0, cursorPosition)
                                + text.substring(cursorPosition + 1);

                        updateText(newText);
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
                    if (multiline) {
                        charInput('\n');
                    } else {
                        UI.select(null);
                    }
                }
            }
        }
    }

    public void charInput(char c) {
        if (active()) {
            String text = uiText.getText();
            boundCursorPosition();

            String newText = text;
            boolean validKey = isValidChar(c);
            if (!validKey)
                return;

            newText = text.substring(0, cursorPosition) + c + text.substring(cursorPosition);
            cursorPosition++;
            updateText(newText);
            resetTimer();
        }
    }

    protected boolean isValidChar(char c) {
        return (c >= 32 && c <= 126) || (c >= 160 && c < 255) || (c == '\n' && multiline);
    }

    /**
     * This method needs be called every time the {@code cursorPosition} variable is
     * used. The reason is that the value represented byy this UITextInput (and thus
     * the String it displays) might have changed since the last time the bounds
     * have been checked.
     */
    private void boundCursorPosition() {
        boundCursorPosition(uiText.getText());
    }

    private void boundCursorPosition(String text) {
        cursorPosition = Math.min(Math.max(cursorPosition, 0), text.length());
    }

    private void updateText(String newText) {
        valueUpdater.accept(newText);
        uiText.syncText();
    }

    @Override
    public void select(SVector mouse) {
        if (mouse == null) {
            cursorPosition = uiText.getText().length();
        } else {
            cursorPosition = uiText.getCharIndex(mouse.x - position.x - uiText.getPosition().x);
        }
    }

    public SVector getCursorPosition() {
        boundCursorPosition();
        SVector pos = new SVector(uiText.getPosition());
        pos.add(position);
        pos.x += uiText.textWidth(cursorPosition);
        return pos;
    }

    public SVector getCursorSize() {
        return new SVector(UISizes.STROKE_WEIGHT.get(), uiText.getTextSize());
    }

    public boolean isCursorVisible() {
        return active() && ((System.nanoTime() * 1e-9 - blinkStart) / BLINK_INTERVAL) % 2 < 1;
    }

    public void resetTimer() {
        blinkStart = System.nanoTime() * 1e-9;
    }

    private boolean active() {
        return UI.getSelectedElement() == this;
    }
}