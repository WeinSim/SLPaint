package ui;

import java.util.function.Consumer;
import java.util.function.Supplier;

import main.apps.ResizeApp;
import sutil.ui.UIButton;
import sutil.ui.UIContainer;
import sutil.ui.UINumberInput;
import sutil.ui.UIText;

public class ResizeUI extends AppUI<ResizeApp> {

    public ResizeUI(ResizeApp app) {
        super(app);
    }

    @Override
    protected void init() {
        root.setMinimalSize();

        root.setMarginScale(2.0);
        root.setPaddingScale(1.0);
        root.setAlignment(UIContainer.RIGHT);

        for (int i = 0; i < 2; i++) {
            UIContainer row = new UIContainer(UIContainer.HORIZONTAL, UIContainer.CENTER);
            row.setHFillSize().zeroMargin().noOutline();

            row.add(new UIText(i == 0 ? "Width:" : "Height:"));
            row.add(new UIContainer(0, 0).setHFillSize().noOutline());
            Supplier<Integer> getter = i == 0 ? app::getNewImageWidth : app::getNewImageHeight;
            Consumer<Integer> setter = i == 0 ? app::setNewImageWidth : app::setNewImageHeight;
            row.add(new UINumberInput(getter, setter));
            root.add(row);
        }

        root.add(new UIButton("Resize image", app::done));
    }
}