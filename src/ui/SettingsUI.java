package ui;

import main.ColorPicker;
import main.apps.App;
import main.apps.MainApp;
import main.apps.SettingsApp;
import sutil.ui.UIButton;
import sutil.ui.UIContainer;
import sutil.ui.UIContextMenu;
import sutil.ui.UIDropdown;
import sutil.ui.UIFloatMenu;
import sutil.ui.UILabel;
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

        UIContainer mainContainer = new UIContainer(UIContainer.VERTICAL, UIContainer.LEFT, UIContainer.BOTH);
        mainContainer.setMaximalSize();

        mainContainer.add(createBaseColor());
        mainContainer.add(createDarkModeDropdown());
        mainContainer.add(new UISeparator());
        mainContainer.add(createHueSatDropdown());
        mainContainer.add(createTestDropdown());

        UIContextMenu contextMenu = new UIContextMenu(this, false);
        contextMenu.addLabel("Label 1", () -> System.out.println("Label 1"));
        contextMenu.addSeparator();

        UIFloatMenu nestedMenu = new UIFloatMenu(this, null, null, false);
        nestedMenu.addLabel("Nested", null);
        contextMenu.addNestedContextMenu("Nested Menu", nestedMenu);
        contextMenu.attachToContainer(mainContainer);

        root.add(mainContainer.addScrollBars());

        root.add(new UIButton("Done", () -> app.getWindow().requestClose()));
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
        UIColorElement baseColorButton = new UIColorElement(
                () -> MainApp.toInt(Colors.getBaseColor()),
                Sizes.COLOR_BUTTON.size, true);
        baseColorHeading.add(baseColorButton);
        baseColorHeading.setLeftClickAction(() -> colorSelectionExpanded = !colorSelectionExpanded);
        baseColor.add(baseColorHeading);

        UIContainer allColorsContainer = new UIContainer(UIContainer.HORIZONTAL, UIContainer.TOP);
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
        CustomColorContainer ccc = new CustomColorContainer(app.getCustomColorButtonArray(),
                color -> {
                    if (color == null) {
                        return;
                    }
                    app.setUIColor(color);
                });
        ccc.zeroMargin().noOutline();
        customColors.add(ccc);
        customColors.add(new UIText("Custom Colors"));
        allColorsContainer.add(customColors);

        baseColor.add(allColorsContainer);

        ColorPicker colorPicker = app.getColorPicker();
        ColorPickContainer colorPickContainer = new ColorPickContainer(
                colorPicker,
                Sizes.COLOR_PICKER_SIDE_PANEL.size,
                UIContainer.HORIZONTAL, false, true);
        colorPickContainer.setVisibilitySupplier(() -> colorSelectionExpanded);
        baseColor.add(colorPickContainer);

        UIContainer innerScrollArea = new UIContainer(UIContainer.VERTICAL, UIContainer.LEFT, UIContainer.VERTICAL);
        innerScrollArea.setVFixedSize(200);
        for (int i = 0; i < 10; i++) {
            innerScrollArea.add(new UILabel(String.format("Label %d", i)));
        }
        baseColor.add(innerScrollArea.addScrollBars());

        return baseColor;
    }

    private UIContainer createDarkModeDropdown() {
        UIContainer container = new UIContainer(UIContainer.HORIZONTAL,
                UIContainer.CENTER);
        container.zeroMargin().noOutline();

        container.add(new UIText("Theme:"));
        container.add(new UIDropdown(
                this,
                new String[] { "Dark", "Light" },
                () -> Colors.isDarkMode() ? 0 : 1,
                i -> app.setDarkMode(i == 0)));
        return container;
    }

    private UIContainer createHueSatDropdown() {
        UIContainer container = new UIContainer(UIContainer.HORIZONTAL, UIContainer.CENTER);
        container.zeroMargin().noOutline();

        container.add(new UIText("Shape of Hue-Saturation Field:"));
        container.add(new UIDropdown(this,
                new String[] { "Circle", "Square" },
                () -> App.isCircularHueSatField() ? 0 : 1,
                i -> App.setCircularHueSatField(i == 0)));
        return container;
    }

    private UIContainer createTestDropdown() {
        UIContainer container = new UIContainer(UIContainer.HORIZONTAL, UIContainer.CENTER);
        container.zeroMargin().noOutline();

        container.add(new UIText("Dropdown menu with scroll area:"));
        container.add(new UIDropdown(
                this,
                new String[] { "There", "are", "a", "lot", "of", "options" },
                () -> 0,
                _ -> {
                }, true));
        return container;
    }
}