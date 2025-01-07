package sutil.ui;

import org.lwjgl.glfw.GLFW;

import sutil.math.SVector;

public class UITextInput extends UIContainer {

    private boolean numberInput;

    private UISetter<String> valueUpdater;

    public UITextInput(UIGetter<String> textUpdater, UISetter<String> valueUpdater) {
        super(HORIZONTAL, CENTER);
        this.valueUpdater = valueUpdater;

        add(new UIText(() -> textUpdater.get()));

        outlineNormal = true;
        add(new Cursor());

        setClickAction(() -> {
            panel.setSelectedElement(this);
            showCursor();
        });
    }

    @Override
    public void keyPressed(char key) {
        if (active()) {
            UIText uiText = (UIText) children.get(0);
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
                boolean validKey = false;
                if (numberInput) {
                    validKey = key >= '0' && key <= '9';
                } else {
                    if (key >= 32 && key <= 126)
                        validKey = true;
                    if (key >= 160 && key < 255)
                        validKey = true;
                }
                if (!validKey)
                    return;
                newText = text + key;
            }
            // uiText.setText(newText);
            valueUpdater.set(newText);

            showCursor();
        }
    }

    private boolean active() {
        return panel.getSelectedElement() == this;
    }

    private void showCursor() {
        ((Cursor) children.get(1)).resetTimer();
    }

    @Override
    public void setMinSize() {
        super.setMinSize();
        // size.x = 60;
        size.x = panel.getTextSize() * 3.3333;
    }

    @Override
    public boolean isSelectable() {
        return true;
    }

    @Override
    public double getMargin() {
        return super.getMargin() / 2;
    }

    @Override
    public double getPadding() {
        return super.getPadding() / 3;
    }

    public String getText() {
        return ((UIText) children.get(0)).getText();
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
            setFillSize();

            hide();
        }

        public void resetTimer() {
            blinkStart = System.nanoTime() * 1e-9;
        }

        public void hide() {
        }

        @Override
        public void update(SVector mouse) {
            super.update(mouse);

            outlineNormal = active() && ((System.nanoTime() * 1e-9 - blinkStart) / BLINK_INTERVAL) % 2 < 1;
        }
    }
}