package ui;

import main.ColorPicker;
import main.apps.App;
import main.apps.MainApp;
import main.apps.SettingsApp;
import sutil.math.SVector;
import sutil.ui.UIContainer;
import sutil.ui.UISeparator;
import sutil.ui.UIText;
import ui.components.ColorButton;
import ui.components.ColorPickContainer;
import ui.components.CustomColorContainer;

public class SettingsUI extends AppUI<SettingsApp> {

    public static final int NUM_UI_BASE_COLOR_BUTTONS = 10;

    public SettingsUI(SettingsApp app) {
        super(app);
    }

    @Override
    protected void init() {
        root.setOrientation(UIContainer.VERTICAL);
        root.setZeroMargin(false);
        root.setZeroPadding(false);
        root.setMinimalSize();

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
        ColorButton baseColorButton = new ColorButton(() -> MainApp.toInt(App.getBaseColor()),
                MainUI.COLOR_BUTTON_SIZE);
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
        for (SVector color : App.DEFAULT_UI_COLORS) {
            int colorInt = MainApp.toInt(color);
            ColorButton button = new ColorButton(() -> colorInt, MainUI.COLOR_BUTTON_SIZE);
            button.setClickAction(() -> app.setUIColor(colorInt));
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

        baseColorExpand.add(new ColorPickContainer(colorPicker, 240, UIContainer.HORIZONTAL));

        baseColorButton.setClickAction(() -> app.queueEvent(() -> {
            if (baseColor.getChildren().contains(baseColorExpand)) {
                baseColor.remove(baseColorExpand);
            } else {
                baseColor.add(baseColorExpand);
            }
        }));

        root.add(baseColor);
    }
}