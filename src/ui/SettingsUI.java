package ui;

import main.ColorPicker;
import main.apps.App;
import main.apps.MainApp;
import main.apps.SettingsApp;
import sutil.ui.UIButton;
import sutil.ui.UIContainer;
import sutil.ui.UILabel;
import sutil.ui.UIScrollArea;
import sutil.ui.UISeparator;
import sutil.ui.UIText;
import sutil.ui.UIToggle;
import ui.components.ColorPickContainer;
import ui.components.CustomColorContainer;
import ui.components.SeparatorContainer;
import ui.components.UIColorElement;

public class SettingsUI extends AppUI<SettingsApp> {

    public static final int NUM_UI_BASE_COLOR_BUTTONS = 10;

    public SettingsUI(SettingsApp app) {
        super(app);
    }

    @Override
    protected void init() {
        root.setZeroMargin(false);
        root.setZeroPadding(false);

        UIScrollArea mainContainer = new UIScrollArea(UIContainer.VERTICAL, UIContainer.LEFT, UIScrollArea.BOTH);

        for (int i = 0; i < 7; i++) {
            mainContainer.add(new UILabel(String.format("Label %d", i)));
        }

        mainContainer.add(createBaseColor());
        mainContainer.add(createDarkModeToggle());
        mainContainer.add(createHueSatFieldToggle());

        for (int i = 0; i < 7; i++) {
            mainContainer.add(new UILabel(String.format("Label %d", i)));
        }

        // root.add(mainContainer);
        root.add(mainContainer.addScrollBars());

        root.add(new UIButton("Done", () -> app.getWindow().requestClose()));
    }

    private UIContainer createBaseColor() {
        UIContainer baseColor = new UIContainer(UIContainer.VERTICAL, UIContainer.LEFT);
        // baseColor.setFillSize();
        baseColor.setMinimalSize();

        UIContainer baseColorHeading = new UIContainer(UIContainer.HORIZONTAL, UIContainer.CENTER);
        baseColorHeading.zeroMargin().noOutline();
        baseColorHeading.setFillSize();
        baseColorHeading.add(new UIText("UI Base Color"));
        UIContainer gap = new UIContainer(0, 0).zeroMargin().zeroPadding();
        gap.noOutline();
        // gap.setMaximalSize();
        baseColorHeading.add(gap);
        UIColorElement baseColorButton = new UIColorElement(() -> MainApp.toInt(Colors.getBaseColor()),
                Sizes.COLOR_BUTTON.size, true);
        baseColorHeading.add(baseColorButton);
        for (int i = 0; i <= 24; i++) {
            baseColorHeading.add(new UILabel(Integer.toString(1 << i)));
        }
        // baseColorRow1.add(new UIButton("Reset", () ->
        // colorPicker.setRGB(MainApp.toInt(App.DEFAULT_BASE_COLOR))));
        baseColor.add(baseColorHeading);

        SeparatorContainer baseColorExpand = new SeparatorContainer(UIContainer.VERTICAL, UIContainer.LEFT);

        UIContainer allColorsContainer = new UIContainer(UIContainer.HORIZONTAL, UIContainer.TOP);
        allColorsContainer.zeroMargin().noOutline();

        UIContainer defaultColors = new UIContainer(UIContainer.VERTICAL, UIContainer.CENTER);
        defaultColors.zeroMargin().noOutline();
        UIContainer defaultColorContainer = new UIContainer(UIContainer.HORIZONTAL, UIContainer.CENTER);
        defaultColorContainer.zeroMargin().noOutline();
        int numDefaultColors = Colors.getNumDefaultColors();
        for (int i = 0; i < numDefaultColors; i++) {
            final int j = i;
            UIColorElement button = new UIColorElement(() -> MainApp.toInt(Colors.getDefaultColors()[j]),
                    Sizes.COLOR_BUTTON.size, true);
            button.setClickAction(() -> app.setUIColor(MainApp.toInt(Colors.getDefaultColors()[j])));
            defaultColorContainer.add(button);
        }
        defaultColors.add(defaultColorContainer);
        defaultColors.add(new UIText("Default Colors"));
        allColorsContainer.add(defaultColors);

        allColorsContainer.add(new UISeparator());

        UIContainer customColors = new UIContainer(UIContainer.VERTICAL, UIContainer.CENTER);
        customColors.zeroMargin().noOutline();
        CustomColorContainer ccc = new CustomColorContainer(app.getCustomColorButtonArray(),
                (Integer color) -> {
                    if (color == null) {
                        return;
                    }
                    app.setUIColor(color);
                });
        ccc.zeroMargin().noOutline();
        customColors.add(ccc);
        customColors.add(new UIText("Custom Colors"));
        allColorsContainer.add(customColors);

        baseColorExpand.add(allColorsContainer);

        ColorPicker colorPicker = app.getColorPicker();
        baseColorExpand.add(new ColorPickContainer(colorPicker, Sizes.COLOR_PICKER_SIDE_PANEL.size,
                UIContainer.HORIZONTAL, false, true));

        baseColorButton.setClickAction(() -> app.queueEvent(() -> {
            if (baseColor.getChildren().contains(baseColorExpand)) {
                baseColor.remove(baseColorExpand);
            } else {
                baseColor.add(baseColorExpand);
            }
        }));

        return baseColor;
    }

    private UIContainer createDarkModeToggle() {
        // UIButton button = new UIButton("", () -> app.toggleDarkMode());
        // button.setText(() -> String.format("Mode: %s", Colors.isDarkMode() ? "Dark" :
        // "Light"));
        // return button;

        UIContainer container = new UIContainer(UIContainer.HORIZONTAL,
                UIContainer.CENTER);
        UIText label = new UIText(() -> String.format("Mode: %s",
                Colors.isDarkMode()
                        ? "Dark"
                        : "Light"));
        container.add(label);
        UIToggle toggle = new UIToggle(() -> Colors.isDarkMode(), _ -> app.toggleDarkMode());
        container.add(toggle);
        return container;
    }

    private UIContainer createHueSatFieldToggle() {
        // UIButton button = new UIButton("", () -> App.toggleCircularHueSatField());
        // button.setText(() -> String.format("HueSatField: %s",
        // App.isCircularHueSatField() ? "Circle" : "Square"));
        // return button;

        UIContainer container = new UIContainer(UIContainer.HORIZONTAL, UIContainer.CENTER);
        UIText label = new UIText(() -> String.format("HueSatField: %s",
                App.isCircularHueSatField()
                        ? "Circle"
                        : "Square"));
        container.add(label);
        UIToggle toggle = new UIToggle(() -> App.isCircularHueSatField(),
                _ -> App.toggleCircularHueSatField());
        container.add(toggle);
        return container;
    }
}