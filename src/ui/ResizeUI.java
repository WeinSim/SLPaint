package ui;

import java.util.function.Consumer;
import java.util.function.Supplier;

import main.apps.ResizeApp;
import sutil.ui.UIButton;
import sutil.ui.UIContainer;
import sutil.ui.UILabel;
import sutil.ui.UINumberInput;
import sutil.ui.UIRadioButtonList;
import sutil.ui.UIText;

public class ResizeUI extends AppUI<ResizeApp> {

    private int sizeMode;

    public ResizeUI(ResizeApp app) {
        super(app);

        sizeMode = ResizeApp.PIXELS;
    }

    @Override
    protected void init() {
        root.setMinimalSize();

        root.setMarginScale(2.0);
        root.setPaddingScale(1.0);
        root.setAlignment(UIContainer.CENTER);

        root.add(new UILabel("Resize"));

        UIContainer mode = new UIContainer(UIContainer.HORIZONTAL, UIContainer.CENTER);
        mode.zeroMargin().noOutline();
        mode.add(new UIText("By:"));
        UIRadioButtonList radioButtons = new UIRadioButtonList(
                UIContainer.HORIZONTAL,
                new String[] { "Pixels", "Percentage" },
                this::getSizeMode,
                this::setSizeMode);
        mode.add(radioButtons);
        root.add(mode);

        UIContainer absolute = new UIContainer(UIContainer.VERTICAL, UIContainer.CENTER);
        absolute.setHFillSize();

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

            pixelInput.setVisibilitySupplier(() -> getSizeMode() == ResizeApp.PIXELS);
            percentageInput.setVisibilitySupplier(() -> getSizeMode() == ResizeApp.PERCENTAGE);

            if (i == 0)
                app.setWidthInput(pixelInput);

            row.add(pixelInput);
            row.add(percentageInput);
            absolute.add(row);
        }

        root.add(absolute);

        UIContainer buttonRow = new UIContainer(UIContainer.HORIZONTAL, UIContainer.RIGHT, UIContainer.CENTER);
        buttonRow.setHFillSize().zeroMargin().noOutline();

        buttonRow.add(new UIButton("OK", app::done));
        buttonRow.add(new UIButton("Cancel", app::cancel));

        root.add(buttonRow);
    }

    public int getSizeMode() {
        return sizeMode;
    }

    public void setSizeMode(int sizeMode) {
        this.sizeMode = sizeMode;
    }
}