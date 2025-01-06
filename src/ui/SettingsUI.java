package ui;

import main.ColorPicker;
import main.apps.App;
import main.apps.MainApp;
import main.apps.SettingsApp;
import sutil.ui.UIButton;
import sutil.ui.UIContainer;
import sutil.ui.UISeparator;
import sutil.ui.UIText;
import ui.components.ColorPickContainer;
import ui.components.CustomColorContainer;
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

        UIContainer mainContainer = new UIContainer(UIContainer.VERTICAL, UIContainer.LEFT);
        mainContainer.setMaximalSize();

        mainContainer.add(createBaseColor());
        mainContainer.add(createDarkModeToggle());
        mainContainer.add(createHueSatFieldToggle());

        root.add(mainContainer);

        root.add(new UIButton("Done", () -> app.getWindow().requestClose()));
    }

    private UIContainer createBaseColor() {
        UIContainer baseColor = new UIContainer(UIContainer.VERTICAL, UIContainer.LEFT);
        baseColor.setFillSize();

        UIContainer baseColorHeading = new UIContainer(UIContainer.HORIZONTAL, UIContainer.CENTER);
        baseColorHeading.zeroMargin().noOutline();
        baseColorHeading.setFillSize();
        baseColorHeading.add(new UIText("UI Base Color"));
        UIContainer gap = new UIContainer(0, 0).zeroMargin().zeroPadding();
        gap.noOutline();
        // gap.setMaximalSize();
        baseColorHeading.add(gap);
        UIColorElement baseColorButton = new UIColorElement(() -> MainApp.toInt(Colors.getBaseColor()),
                Sizes.getColorButtonSize(), true);
        baseColorHeading.add(baseColorButton);
        ColorPicker colorPicker = app.getColorPicker();
        // baseColorRow1.add(new UIButton("Reset", () ->
        // colorPicker.setRGB(MainApp.toInt(App.DEFAULT_BASE_COLOR))));
        baseColor.add(baseColorHeading);

        UIContainer baseColorExpand = new UIContainer(UIContainer.VERTICAL, UIContainer.LEFT) {
            @Override
            public double getMargin() {
                return 2 * super.getMargin();
            }
        };

        UIContainer allColorsContainer = new UIContainer(UIContainer.HORIZONTAL, UIContainer.TOP);
        allColorsContainer.zeroMargin().noOutline();

        UIContainer defaultColors = new UIContainer(UIContainer.VERTICAL, UIContainer.CENTER);
        defaultColors.zeroMargin().noOutline();
        UIContainer defaultColorContainer = new UIContainer(UIContainer.HORIZONTAL, UIContainer.CENTER);
        defaultColorContainer.zeroMargin().noOutline();
        // for (SVector color : Colors.DEFAULT_UI_COLORS_DARK) {
        // int colorInt = MainApp.toInt(color);
        // ColorButton button = new ColorButton(() -> colorInt,
        // MainUI.COLOR_BUTTON_SIZE);
        // button.setClickAction(() -> app.setUIColor(colorInt));
        // defaultColorContainer.add(button);
        // }
        int numDefaultColors = Colors.getNumDefaultColors();
        for (int i = 0; i < numDefaultColors; i++) {
            final int j = i;
            UIColorElement button = new UIColorElement(() -> MainApp.toInt(Colors.getDefaultColors()[j]),
                    Sizes.getColorButtonSize(), true);
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

        baseColorExpand.add(new UISeparator());

        baseColorExpand.add(new ColorPickContainer(colorPicker, Sizes.getColorPickerSizeSidePanel(),
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
        // UIContainer darkModeToggle = new UIContainer(UIContainer.HORIZONTAL,
        // UIContainer.CENTER);

        UIButton button = new UIButton("", () -> app.toggleDarkMode());
        button.setText(() -> String.format("Mode: %s", Colors.isDarkMode() ? "Dark" : "Light"));

        return button;

        // darkModeToggle.add(button);
        // return darkModeToggle;
    }

    private UIContainer createHueSatFieldToggle() {
        // UIContainer hueSatToggle = new UIContainer(UIContainer.HORIZONTAL,
        // UIContainer.CENTER);

        UIButton button = new UIButton("", () -> App.toggleCircularHueSatField());
        button.setText(() -> String.format("HueSatField: %s", App.isCircularHueSatField() ? "Circle" : "Square"));

        return button;

        // hueSatToggle.add(button);
        // return hueSatToggle;
    }
}