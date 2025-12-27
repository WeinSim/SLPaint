package sutil.ui;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class UINumberInput extends UITextInput {

    public UINumberInput(Supplier<Integer> getter, Consumer<Integer> setter) {
        super(
                () -> Integer.toString(getter.get()),
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