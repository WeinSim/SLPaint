package ui;

import main.ColorPicker;
import main.apps.App;
import main.apps.MainApp;
import main.apps.SettingsApp;
import main.settings.Settings;
import sutil.ui.UIButton;
import sutil.ui.UIContainer;
import sutil.ui.UIContextMenu;
import sutil.ui.UIDropdown;
import sutil.ui.UIFloatMenu;
import sutil.ui.UISeparator;
import sutil.ui.UIText;
import ui.components.ColorPickContainer;
import ui.components.CustomColorContainer;
import ui.components.UIColorElement;

public class SettingsUI extends AppUI<SettingsApp> {

    public static final int NUM_UI_BASE_COLOR_BUTTONS = 10;

    private boolean colorSelectionExpanded;

    public SettingsUI(SettingsApp app) {
        super(app);

        colorSelectionExpanded = false;
    }

    @Override
    protected void init() {
        root.setMarginScale(1);
        root.setPaddingScale(1);

        UIContainer mainContainer = new UIContainer(UIContainer.VERTICAL, UIContainer.LEFT, UIContainer.TOP,
                UIContainer.BOTH);
        mainContainer.setFillSize();

        mainContainer.add(createBaseColor());
        mainContainer.add(createDarkModeDropdown());
        mainContainer.add(createHueSatDropdown());
        mainContainer.add(createHSLHSVDropdown());

        UIContextMenu contextMenu = new UIContextMenu(false);
        contextMenu.addLabel("Label 1", () -> System.out.println("Label 1"));
        contextMenu.add(new UISeparator());

        UIFloatMenu nestedMenu = new UIFloatMenu(true);
        for (int i = 0; i < 20; i++) {
            nestedMenu.addLabel(String.format("Nested %d", i), null);
        }
        contextMenu.addNestedContextMenu("Nested Menu", nestedMenu);
        contextMenu.attachToContainer(mainContainer);

        root.add(mainContainer.addScrollbars());

        UIContainer bottomRow = new UIContainer(UIContainer.HORIZONTAL, UIContainer.TOP);
        bottomRow.setHFillSize().zeroMargin().noOutline();
        bottomRow.add(new UIButton("Done", () -> app.getWindow().requestClose()));
        UIContainer fill = new UIContainer(0, 0);
        fill.setHFillSize().noOutline();
        bottomRow.add(fill);
        bottomRow.add(new UIButton("Reset Settings", () -> Settings.loadDefaultSettings()));

        root.add(bottomRow);
    }

    private UIContainer createBaseColor() {
        UIContainer baseColor = new UIContainer(UIContainer.VERTICAL, UIContainer.LEFT);
        baseColor.withSeparators();

        UIContainer baseColorHeading = new UIContainer(UIContainer.HORIZONTAL, UIContainer.LEFT, UIContainer.CENTER);
        baseColorHeading.zeroMargin().noOutline();
        baseColorHeading.setBackgroundHighlight(true);
        baseColorHeading.setHFillSize();
        baseColorHeading.add(new UIText("UI Base Color:"));
        UIContainer gap = new UIContainer(0, 0).zeroMargin().zeroPadding();
        gap.noOutline();
        baseColorHeading.add(gap);
        UIColorElement baseColorButton = new UIColorElement(Colors::getBaseColor, Sizes.COLOR_BUTTON.size, true);
        baseColorHeading.add(baseColorButton);
        baseColorHeading.setLeftClickAction(() -> colorSelectionExpanded = !colorSelectionExpanded);
        baseColor.add(baseColorHeading);

        UIContainer allColorsContainer = new UIContainer(UIContainer.HORIZONTAL, UIContainer.LEFT, UIContainer.CENTER);
        allColorsContainer.zeroMargin().noOutline();
        allColorsContainer.setVisibilitySupplier(() -> colorSelectionExpanded);

        UIContainer defaultColors = new UIContainer(UIContainer.VERTICAL, UIContainer.CENTER);
        defaultColors.zeroMargin().noOutline();
        UIContainer defaultColorContainer = new UIContainer(UIContainer.HORIZONTAL, UIContainer.CENTER);
        defaultColorContainer.zeroMargin().noOutline();
        int numDefaultColors = Colors.getNumDefaultColors();
        for (int i = 0; i < numDefaultColors; i++) {
            final int j = i;
            UIColorElement button = new UIColorElement(() -> MainApp.toInt(Colors.getDefaultColors()[j]),
                    Sizes.COLOR_BUTTON.size, true);
            button.setLeftClickAction(() -> app.setUIColor(MainApp.toInt(Colors.getDefaultColors()[j])));
            defaultColorContainer.add(button);
        }
        defaultColors.add(defaultColorContainer);
        defaultColors.add(new UIText("Default Colors"));
        allColorsContainer.add(defaultColors);

        allColorsContainer.add(new UISeparator());

        UIContainer customColors = new UIContainer(UIContainer.VERTICAL, UIContainer.CENTER);
        customColors.zeroMargin().noOutline();
        CustomColorContainer ccc = new CustomColorContainer(MainApp.getCustomUIBaseColors(), app::setUIColor);
        ccc.zeroMargin().noOutline();
        customColors.add(ccc);
        customColors.add(new UIText("Custom Colors"));
        allColorsContainer.add(customColors);

        allColorsContainer.add(new UIButton("Clear", () -> MainApp.getCustomUIBaseColors().clear()));

        baseColor.add(allColorsContainer);

        ColorPicker colorPicker = app.getColorPicker();
        ColorPickContainer colorPickContainer = new ColorPickContainer(
                colorPicker,
                MainApp::addCustomUIBaseColor,
                Sizes.COLOR_PICKER_SIDE_PANEL.size,
                UIContainer.HORIZONTAL, false, true);
        colorPickContainer.setVisibilitySupplier(() -> colorSelectionExpanded);
        baseColor.add(colorPickContainer);

        return baseColor;
    }

    private UIContainer createDarkModeDropdown() {
        UIContainer container = new UIContainer(UIContainer.HORIZONTAL,
                UIContainer.CENTER);
        container.zeroMargin().noOutline();

        container.add(new UIText("Theme:"));
        container.add(new UIDropdown(
                new String[] { "Dark", "Light" },
                () -> Colors.isDarkMode() ? 0 : 1,
                i -> app.setDarkMode(i == 0)));
        return container;
    }

    private UIContainer createHueSatDropdown() {
        UIContainer container = new UIContainer(UIContainer.HORIZONTAL, UIContainer.CENTER);
        container.zeroMargin().noOutline();

        container.add(new UIText("Shape of Hue-Saturation Field:"));
        container.add(new UIDropdown(
                new String[] { "Circle", "Square" },
                () -> App.isCircularHueSatField() ? 0 : 1,
                i -> App.setCircularHueSatField(i == 0)));
        return container;
    }

    private UIContainer createHSLHSVDropdown() {
        UIContainer container = new UIContainer(UIContainer.HORIZONTAL, UIContainer.CENTER);
        container.zeroMargin().noOutline();

        container.add(new UIText("Color space:"));
        container.add(new UIDropdown(
                new String[] { "HSV", "HSL" },
                () -> App.isHSLColorSpace() ? 1 : 0,
                i -> App.setHSLColorSpace(i == 1)));
        return container;
    }
}