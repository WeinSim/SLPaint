package ui;

import main.Image;
import main.ImageFormat;
import main.ImageTool;
import main.SelectionManager;
import main.apps.MainApp;
import sutil.ui.UIButton;
import sutil.ui.UIContainer;
import sutil.ui.UIGetter;
import sutil.ui.UILabel;
import sutil.ui.UISeparator;
import sutil.ui.UIText;
import ui.components.ColorPickContainer;
import ui.components.CustomColorContainer;
import ui.components.ImageCanvas;
import ui.components.ToolButton;
import ui.components.UIColorElement;

public class MainUI extends AppUI<MainApp> {

    public static final int NUM_COLOR_BUTTONS_PER_ROW = 10;

    public MainUI(MainApp app) {
        super(app);
    }

    @Override
    protected void init() {
        root.setOrientation(UIContainer.VERTICAL);
        root.setAlignment(UIContainer.LEFT);
        root.zeroMargin().zeroPadding().noOutline();

        // UIContainer mainField = new UIContainer(UIContainer.VERTICAL,
        // UIContainer.LEFT);
        // mainField.zeroMargin().zeroPadding().noOutline();

        UIContainer topRow = new UIContainer(UIContainer.HORIZONTAL, UIContainer.CENTER) {
            @Override
            public double getMargin() {
                return 2 * super.getMargin();
            }
        };
        topRow.setFillSize();
        topRow.withBackground().noOutline();

        UIContainer settings = addTopRowSection(topRow, "Settings");
        settings.add(new UIButton("Settings", () -> app.showDialog(MainApp.SETTINGS_DIALOG)));

        topRow.add(new UISeparator());

        UIContainer fileOptions = addTopRowSection(topRow, "File");
        fileOptions.add(new UIButton("New", () -> app.newImage()));
        fileOptions.add(new UIButton("Open", () -> app.openImage()));
        fileOptions.add(new UIButton("Save", () -> app.saveImage()));

        topRow.add(new UISeparator());

        UIContainer imageOptions = addTopRowSection(topRow, "Image");
        imageOptions.add(new UIButton("Change Size", () -> app.showDialog(MainApp.CHANGE_SIZE_DIALOG)));
        imageOptions.add(new UIButton("Rotate", () -> app.showDialog(MainApp.ROTATE_DIALOG)));
        imageOptions.add(new UIButton("Flip", () -> app.showDialog(MainApp.FLIP_DIALOG)));

        topRow.add(new UISeparator());

        UIContainer toolbox = addTopRowSection(topRow, "Tools");
        for (ImageTool tool : ImageTool.values()) {
            toolbox.add(new ToolButton(app, tool));
        }

        topRow.add(new UISeparator());

        addTopRowSection(topRow, "Size");

        topRow.add(new UISeparator());

        for (int i = 0; i < 2; i++) {
            final int index = i;
            UIContainer colorContainer = new UIContainer(UIContainer.VERTICAL, UIContainer.CENTER);
            setButtonStyle2(colorContainer, () -> app.getColorSelection() == index);
            colorContainer.setSelectable(true);

            UIGetter<Integer> cg = i == 0 ? () -> app.getPrimaryColor() : () -> app.getSecondaryColor();
            colorContainer.add(new UIColorElement(cg, Sizes.BIG_COLOR_BUTTON.size, true));
            UILabel label = new UILabel("%s\nColor".formatted(i == 0 ? "Primary" : "Secondary"));
            label.setAlignment(UIContainer.CENTER);
            label.zeroMargin();
            colorContainer.add(label);

            colorContainer.setClickAction(() -> app.setColorSelection(index));

            topRow.add(colorContainer);
        }

        UIContainer allColors = new UIContainer(UIContainer.VERTICAL, UIContainer.LEFT);
        allColors.zeroMargin().noOutline();
        UIContainer currentRow = null;
        for (int i = 0; i < MainApp.DEFAULT_COLORS.length; i++) {
            if (currentRow == null) {
                currentRow = new UIContainer(UIContainer.HORIZONTAL, UIContainer.CENTER);
                currentRow.zeroMargin().noOutline();
            }

            final int color = MainApp.DEFAULT_COLORS[i];
            UIColorElement button = new UIColorElement(() -> color, Sizes.COLOR_BUTTON.size, true);
            button.setClickAction(() -> app.selectColor(color));
            currentRow.add(button);

            if ((i + 1) % NUM_COLOR_BUTTONS_PER_ROW == 0 || i + 1 == MainApp.DEFAULT_COLORS.length) {
                allColors.add(currentRow);
                currentRow = null;
            }
        }
        CustomColorContainer ccc = new CustomColorContainer(app.getCustomColorButtonArray(),
                (Integer color) -> {
                    if (color == null) {
                        return;
                    }
                    app.selectColor(color);
                });
        ccc.zeroMargin().noOutline();
        allColors.add(ccc);
        topRow.add(allColors);

        UILabel newColor = new UILabel("+");
        setButtonStyle1(newColor);
        newColor.setClickAction(() -> app.showDialog(MainApp.NEW_COLOR_DIALOG));
        topRow.add(newColor);

        // mainField.add(topRow);
        root.add(topRow);

        ImageCanvas canvas = new ImageCanvas(UIContainer.VERTICAL, UIContainer.RIGHT, app);
        canvas.noOutline();
        root.add(canvas);

        UIContainer sidePanel = new UIContainer(UIContainer.VERTICAL, UIContainer.LEFT) {
            @Override
            public double getMargin() {
                return 2 * super.getMargin();
            }
        };
        sidePanel.withBackground().noOutline();
        UIButton transparentSelection = new UIButton("", () -> app.toggleTransparentSelection());
        transparentSelection.setText(() -> String.format(
                "Transparent selection: %s",
                app.isTransparentSelection() ? "On" : "Off"));
        sidePanel.add(transparentSelection);

        sidePanel.add(new UISeparator());

        sidePanel.add(new ColorPickContainer(
                app.getSelectedColorPicker(),
                Sizes.COLOR_PICKER_SIDE_PANEL.size,
                UIContainer.VERTICAL,
                true,
                false));
        canvas.add(sidePanel);

        UIContainer statusBar = new UIContainer(UIContainer.HORIZONTAL, UIContainer.BOTTOM);
        // statusBar.zeroMargin().noOutline().withBackground();
        statusBar.withBackground().noOutline();
        statusBar.setFillSize();
        statusBar.add(new UILabel(() -> String.format("%.1f fps", app.getFrameRate())));
        statusBar.add(new UISeparator());
        statusBar.add(new UILabel(() -> {
            String ret = "Format: ";
            ImageFormat format = app.getImageFormat();
            if (format == null) {
                ret += "[no format], ";
            } else {
                ret += format.toString() + ", ";
            }
            String filename = app.getFilename();
            if (filename.length() > 0) {
                long filesize = app.getFilesize();
                ret += "%s (%s)".formatted(filename, MainApp.formatFilesize(filesize));
            }
            return ret;
        }));
        statusBar.add(new UISeparator());
        statusBar.add(new UILabel(() -> {
            Image image = app.getImage();
            int width = image.getWidth();
            int height = image.getHeight();
            return "Size: %d x %d px".formatted(width, height);
        }));
        statusBar.add(new UISeparator());
        statusBar.add(new UILabel(() -> {
            int[] mouseImagePos = app.getMouseImagePosition();
            boolean inside = app.getImage().isInside(mouseImagePos[0], mouseImagePos[1]);
            String ret = "Mouse Position:";
            if (inside) {
                ret += " %d, %d".formatted(mouseImagePos[0], mouseImagePos[1]);
            }
            return ret;
        }));
        statusBar.add(new UISeparator());
        statusBar.add(new UILabel(() -> {
            String ret = "Selection size:";
            SelectionManager selectionManager = app.getSelectionManager();
            if (selectionManager.getPhase() != SelectionManager.NONE) {
                ret += " %d x %d px".formatted(selectionManager.getWidth(),
                        selectionManager.getHeight());
            }
            return ret;
        }));
        UIContainer fill = new UIContainer(0, 0);
        fill.noOutline().noBackground();
        fill.setMaximalSize();
        statusBar.add(fill);
        // canvas.add(statusBar);
        // mainField.add(canvas);
        root.add(statusBar);

        // root.add(mainField);
    }

    private UIContainer addTopRowSection(UIContainer topRow, String name) {
        UIContainer options = new UIContainer(UIContainer.VERTICAL, UIContainer.CENTER);
        options.zeroMargin().noOutline();
        options.setFillSize();

        UIContainer optionButtons = new UIContainer(UIContainer.HORIZONTAL, UIContainer.CENTER);
        optionButtons.zeroMargin().noOutline();
        optionButtons.setMaximalSize();

        options.add(optionButtons);
        options.add(new UIText(name));
        topRow.add(options);
        return optionButtons;
    }
}