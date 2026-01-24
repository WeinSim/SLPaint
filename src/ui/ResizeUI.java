package ui;

import java.util.function.Consumer;
import java.util.function.Supplier;

import main.apps.ResizeApp;
import sutil.ui.UIButton;
import sutil.ui.UIContainer;
import sutil.ui.UINumberInput;
import sutil.ui.UIRadioButtonList;
import sutil.ui.UIText;
import sutil.ui.UIToggle;

public class ResizeUI extends AppUI<ResizeApp> {

    private int inputMode;

    public ResizeUI(ResizeApp app) {
        super(app);

        inputMode = ResizeApp.PIXELS;
    }

    @Override
    protected void init() {
        root.setMinimalSize();

        root.setMarginScale(2.0);
        root.setPaddingScale(1.0);
        root.setAlignment(UIContainer.LEFT);

        UIRadioButtonList resizeMode = new UIRadioButtonList(
                UIContainer.VERTICAL,
                new String[] { "Resize (scale) image", "Crop image" },
                app::getResizeMode,
                app::setResizeMode);
        resizeMode.setMarginScale(1.0).setPaddingScale(2.0);
        root.add(resizeMode);

        UIContainer inner = new UIContainer(UIContainer.VERTICAL, UIContainer.CENTER);
        inner.setHFillSize();

        UIContainer inputMode = new UIContainer(UIContainer.HORIZONTAL, UIContainer.CENTER);
        inputMode.setHFillSize().noOutline();
        // inputMode.zeroMargin();
        inputMode.add(new UIText("By:"));
        // inputMode.add(new UIContainer(0, 0).zeroMargin().setHFillSize().noOutline());
        UIContainer radioButtons = new UIRadioButtonList(
                UIContainer.HORIZONTAL,
                new String[] { "Pixels", "Percentage" },
                this::getInputMode,
                this::setInputMode);
        radioButtons.setPaddingScale(2.0);
        inputMode.add(radioButtons);
        inner.add(inputMode);

        for (int i = 0; i < 2; i++) {
            UIContainer row = new UIContainer(UIContainer.HORIZONTAL, UIContainer.CENTER);
            row.setHFillSize().zeroMargin().noOutline();

            row.add(new UIText(i == 0 ? "Width:" : "Height:"));
            row.add(new UIContainer(0, 0).setHFillSize().noOutline());
            Supplier<Integer> pixelGetter = i == 0 ? app::getWidthPixels : app::getHeightPixels,
                    percentageGetter = i == 0 ? app::getWidthPercentage : app::getHeightPercentage;
            Consumer<Integer> pixelSetter = i == 0 ? app::setWidthPixels : app::setHeightPixels,
                    percentageSetter = i == 0 ? app::setWidthPercentage : app::setHeightPercentage;
            UINumberInput pixelInput = new UINumberInput(pixelGetter, pixelSetter),
                    percentageInput = new UINumberInput(percentageGetter, percentageSetter);

            pixelInput.setVisibilitySupplier(() -> getInputMode() == ResizeApp.PIXELS);
            percentageInput.setVisibilitySupplier(() -> getInputMode() == ResizeApp.PERCENTAGE);

            if (i == 0)
                app.setWidthInput(pixelInput);

            row.add(pixelInput);
            row.add(percentageInput);
            inner.add(row);
        }

        UIContainer ratioContainer = new UIContainer(UIContainer.HORIZONTAL, UIContainer.CENTER);
        ratioContainer.setHFillSize().noOutline();
        ratioContainer.add(new UIText("Lock ratio"));
        ratioContainer.add(new UIContainer(0, 0).setHFillSize().noOutline());
        ratioContainer.add(new UIToggle(app::isLockRatio, app::setLockRatio));
        inner.add(ratioContainer);

        root.add(inner);

        UIContainer buttonRow = new UIContainer(UIContainer.HORIZONTAL, UIContainer.RIGHT, UIContainer.CENTER);
        buttonRow.setHFillSize().zeroMargin().noOutline();

        buttonRow.add(new UIButton("OK", app::done));
        buttonRow.add(new UIButton("Cancel", app::cancel));

        root.add(buttonRow);
    }

    public int getInputMode() {
        return inputMode;
    }

    public void setInputMode(int sizeMode) {
        this.inputMode = sizeMode;
    }
}