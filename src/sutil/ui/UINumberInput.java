package sutil.ui;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class UINumberInput extends UITextInput {

    public UINumberInput(IntSupplier getter, IntConsumer setter) {
        super(
                () -> Integer.toString(getter.getAsInt()),
                (String s) -> {
                    int i = 0;
                    if (s.length() > 0) {
                        try {
                            i = Integer.parseInt(s);
                        } catch (NumberFormatException e) {
                            return;
                        }
                    }
                    setter.accept(i);
                });
    }

    @Override
    public void update() {
        super.update();

        double textSize = uiText.getTextSize();
        setHFixedSize(textSize * 3.3333);
    }

    @Override
    protected boolean isValidChar(char c) {
        return c >= '0' && c <= '9';
    }
}