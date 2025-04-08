package ui;

import java.util.function.Supplier;

import main.Image;
import main.ImageFormat;
import main.apps.MainApp;
import main.tools.ImageTool;
import main.tools.SelectionTool;
import sutil.ui.UIButton;
import sutil.ui.UIContainer;
import sutil.ui.UILabel;
import sutil.ui.UISeparator;
import sutil.ui.UIText;
import sutil.ui.UIToggle;
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
        root.setHAlignment(UIContainer.LEFT);
        root.zeroMargin().zeroPadding().noOutline();

        UIContainer topRow = new UIContainer(UIContainer.HORIZONTAL, UIContainer.LEFT, UIContainer.CENTER,
                UIContainer.HORIZONTAL);
        topRow.withSeparators().setHFillSize().setHAlignment(UIContainer.LEFT).withBackground().noOutline();

        UIContainer settings = addTopRowSection(topRow, "Settings");
        settings.add(new UIButton("Settings", () -> app.showDialog(MainApp.SETTINGS_DIALOG)));

        UIContainer fileOptions = addTopRowSection(topRow, "File");
        fileOptions.add(new UIButton("New", app::newImage));
        fileOptions.add(new UIButton("Open", app::openImage));
        fileOptions.add(new UIButton("Save", app::saveImage));

        UIContainer imageOptions = addTopRowSection(topRow, "Image");
        imageOptions.add(new UIButton("Change Size", () -> app.showDialog(MainApp.CHANGE_SIZE_DIALOG)));
        imageOptions.add(new UIButton("Rotate", () -> app.showDialog(MainApp.ROTATE_DIALOG)));
        imageOptions.add(new UIButton("Flip", () -> app.showDialog(MainApp.FLIP_DIALOG)));

        UIContainer toolbox = addTopRowSection(topRow, "Tools");
        for (ImageTool tool : ImageTool.INSTANCES) {
            toolbox.add(new ToolButton(app, tool));
        }

        addTopRowSection(topRow, "Size");

        for (int i = 0; i < 2; i++) {
            final int index = i;
            UIContainer colorContainer = new UIContainer(UIContainer.VERTICAL, UIContainer.CENTER);
            setButtonStyle2(colorContainer, () -> app.getColorSelection() == index);
            colorContainer.setSelectable(true);

            Supplier<Integer> cg = i == 0 ? app::getPrimaryColor : app::getSecondaryColor;
            colorContainer.add(new UIColorElement(cg, Sizes.BIG_COLOR_BUTTON.size, true));
            UILabel label = new UILabel("%s\nColor".formatted(i == 0 ? "Primary" : "Secondary"));
            label.setAlignment(UIContainer.CENTER);
            label.zeroMargin();
            colorContainer.add(label);

            colorContainer.setLeftClickAction(() -> app.setColorSelection(index));

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
            button.setLeftClickAction(() -> app.selectColor(color));
            currentRow.add(button);

            if ((i + 1) % NUM_COLOR_BUTTONS_PER_ROW == 0 || i + 1 == MainApp.DEFAULT_COLORS.length) {
                allColors.add(currentRow);
                currentRow = null;
            }
        }
        CustomColorContainer ccc = new CustomColorContainer(app.getCustomColorButtonArray(), app::selectColor);
        ccc.zeroMargin().noOutline();
        allColors.add(ccc);
        topRow.add(allColors);

        UIContainer topRowScrollbars = topRow.addScrollbars().setHFillSize();
        root.add(topRowScrollbars);

        ImageCanvas canvas = new ImageCanvas(UIContainer.VERTICAL, UIContainer.RIGHT, UIContainer.TOP, app);
        canvas.noOutline();

        UIContainer sidePanel = new UIContainer(UIContainer.VERTICAL, UIContainer.CENTER, UIContainer.TOP,
                UIContainer.VERTICAL);
        sidePanel.withSeparators().withBackground().noOutline();
        // sidePanel.setVFillSize();
        sidePanel.setVMinimalSize();
        UIContainer transparentSelection = new UIContainer(UIContainer.HORIZONTAL, UIContainer.CENTER);
        transparentSelection.zeroMargin().noOutline();
        transparentSelection.setHFillSize();
        UIContainer fill = new UIContainer(0, 0);
        fill.noOutline();
        fill.setHFillSize();
        transparentSelection.add(new UIText("Transparent selection"));
        transparentSelection.add(fill);
        UIToggle toggle = new UIToggle(MainApp::isTransparentSelection, MainApp::setTransparentSelection);
        transparentSelection.add(toggle);
        sidePanel.add(transparentSelection);

        sidePanel.add(new ColorPickContainer(
                app.getSelectedColorPicker(),
                app::addCustomColor,
                Sizes.COLOR_PICKER_SIDE_PANEL.size,
                UIContainer.VERTICAL,
                true,
                false));
        canvas.add(sidePanel.addScrollbars());

        UIContainer debugPanel = new UIContainer(UIContainer.VERTICAL, UIContainer.LEFT);
        debugPanel.withBackground().noOutline();
        debugPanel.setVFillSize();
        // debugPanel.add(new UIText("Tools"));
        // for (ImageTool tool : ImageTool.INSTANCES) {
        //     debugPanel.add(new UIText(() -> String.format("  %s: state = %d", tool.getName(), tool.getState())));
        // }
        debugPanel.add(new UIText(() -> String.format("Active tool: %s", app.getActiveTool().getName())));
        debugPanel.add(new UIText(() -> String.format("  State: %d", app.getActiveTool().getState())));
        debugPanel.add(new UIText("                           "));
        canvas.add(debugPanel);

        root.add(canvas);

        UIContainer statusBar = new UIContainer(UIContainer.HORIZONTAL, UIContainer.LEFT, UIContainer.CENTER);
        statusBar.withBackground().noOutline();
        statusBar.setHFillSize();
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
            SelectionTool selectionManager = ImageTool.SELECTION;
            if (app.getActiveTool() == selectionManager
                    && selectionManager.getState() != ImageTool.NONE) {
                ret += " %d x %d px".formatted(selectionManager.getWidth(), selectionManager.getHeight());
            }
            return ret;
        }));
        // canvas.add(statusBar);
        // mainField.add(canvas);
        root.add(statusBar);

        // root.add(mainField);
    }

    private UIContainer addTopRowSection(UIContainer topRow, String name) {
        UIContainer options = new UIContainer(UIContainer.VERTICAL, UIContainer.CENTER);
        options.zeroMargin().noOutline();
        options.setVFillSize();

        UIContainer optionButtons = new UIContainer(UIContainer.HORIZONTAL, UIContainer.CENTER);
        optionButtons.zeroMargin().noOutline();
        // optionButtons.setMaximalSize();

        options.add(optionButtons);
        options.add(new UIText(name));
        topRow.add(options);
        return optionButtons;
    }
}