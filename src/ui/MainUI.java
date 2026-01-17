package ui;

import java.util.function.Consumer;
import java.util.function.Supplier;

import main.Image;
import main.ImageFormat;
import main.apps.MainApp;
import main.tools.DragTool;
import main.tools.ImageTool;
import main.tools.PencilTool;
import main.tools.SelectionTool;
import main.tools.TextTool;
import sutil.SUtil;
import sutil.math.SVector;
import sutil.ui.UIButton;
import sutil.ui.UIContainer;
import sutil.ui.UIDropdown;
import sutil.ui.UIElement;
import sutil.ui.UIImage;
import sutil.ui.UILabel;
import sutil.ui.UINumberInput;
import sutil.ui.UIRadioButtonList;
import sutil.ui.UIScale;
import sutil.ui.UISeparator;
import sutil.ui.UISizes;
import sutil.ui.UIText;
import sutil.ui.UIToggle;
import ui.components.ColorPickContainer;
import ui.components.CustomColorContainer;
import ui.components.ImageCanvas;
import ui.components.ToolButton;
import ui.components.UIColorElement;

public class MainUI extends AppUI<MainApp> {

    public static final int NUM_COLOR_BUTTONS_PER_ROW = 10;

    private String debugString = "";
    private int test = 0;

    public MainUI(MainApp app) {
        super(app);
    }

    @Override
    protected void init() {
        root.setOrientation(UIContainer.VERTICAL);
        root.setHAlignment(UIContainer.LEFT);
        // root.withSeparators(false).noOutline();

        UIContainer topRow = new UIContainer(UIContainer.HORIZONTAL, UIContainer.LEFT, UIContainer.CENTER,
                UIContainer.HORIZONTAL);
        topRow.withSeparators(true).setHFillSize().setHAlignment(UIContainer.LEFT).withBackground().noOutline();

        UIContainer settings = addTopRowSection(topRow, "Settings");
        UIButton settingsButton = new UIButton("Settings", () -> app.showDialog(MainApp.SETTINGS_DIALOG));
        // setButtonStyle1(settingsButton);
        settings.add(settingsButton);

        UIContainer fileOptions = addTopRowSection(topRow, "File");
        fileOptions.add(new UILabel("..."));
        // fileOptions.add(new UIButton("New", app::newImage));
        // fileOptions.add(new UIButton("Open", app::openImage));
        // fileOptions.add(new UIButton("Save", app::saveImage));

        UIContainer imageOptions = addTopRowSection(topRow, "Image");
        // imageOptions.add(new UILabel("..."));
        imageOptions.add(new UIButton("Resize", () -> app.showDialog(MainApp.RESIZE_DIALOG)));
        // imageOptions.add(new UIButton("Rotate", () ->
        // app.showDialog(MainApp.ROTATE_DIALOG)));
        // imageOptions.add(new UIButton("Flip", () ->
        // app.showDialog(MainApp.FLIP_DIALOG)));

        UIContainer toolbox = addTopRowSection(topRow, "Tools");
        for (ImageTool tool : ImageTool.INSTANCES) {
            toolbox.add(new ToolButton(app, tool));
        }

        UIContainer pencilTools = new UIContainer(UIContainer.VERTICAL, UIContainer.CENTER);
        pencilTools.zeroMargin().noOutline();
        pencilTools.setVisibilitySupplier(() -> app.getActiveTool() == ImageTool.PENCIL);

        final double min = PencilTool.MIN_SIZE,
                max = PencilTool.MAX_SIZE;
        UIScale pencilSizeScale = new UIScale(UIContainer.HORIZONTAL,
                () -> SUtil.map(ImageTool.PENCIL.getSize(), min, max, 0, 1),
                x -> ImageTool.PENCIL.setSize((int) Math.round(SUtil.map(x, 0, 1, min, max))));
        pencilTools.add(pencilSizeScale);

        UIContainer pencilSizeBottomRow = new UIContainer(UIContainer.HORIZONTAL, UIContainer.CENTER);
        pencilSizeBottomRow.zeroMargin().noOutline();
        pencilSizeBottomRow.add(new UIText("Size:"));
        pencilSizeBottomRow.add(new UIContainer(0, 0).setHFillSize().noOutline());
        pencilSizeBottomRow.add(createIntPicker(ImageTool.PENCIL::getSize, ImageTool.PENCIL::setSize));

        pencilTools.add(pencilSizeBottomRow);
        topRow.add(pencilTools);

        UIContainer selectionTools = new UIContainer(UIContainer.VERTICAL, UIContainer.LEFT);
        selectionTools.zeroMargin().noOutline();
        selectionTools.setVisibilitySupplier(() -> app.getActiveTool() == ImageTool.SELECTION);

        selectionTools.add(createToggleContainer(
                "Transparent selection",
                MainApp::isTransparentSelection,
                MainApp::setTransparentSelection));

        selectionTools.add(createToggleContainer(
                "Lock selection ratio",
                MainApp::isLockSelectionRatio,
                MainApp::setLockSelectionRatio));

        topRow.add(selectionTools);

        UIContainer textTools = new UIContainer(UIContainer.HORIZONTAL, UIContainer.CENTER);
        textTools.zeroMargin().setVFillSize().noOutline();
        textTools.setVisibilitySupplier(() -> app.getActiveTool() == ImageTool.TEXT);
        textTools.setPaddingScale(2);

        UIContainer textSizeContainer = new UIContainer(UIContainer.VERTICAL, UIContainer.CENTER);
        textSizeContainer.zeroMargin().setPaddingScale(2).setVFillSize().noOutline();
        textSizeContainer.add(createIntPicker(ImageTool.TEXT::getSize, ImageTool.TEXT::setSize));
        textSizeContainer.add(new UIText("Size"));
        textTools.add(textSizeContainer);

        UIContainer textFontContainer = new UIContainer(UIContainer.VERTICAL, UIContainer.CENTER);
        textFontContainer.zeroMargin().setPaddingScale(2).setVFillSize().noOutline();
        UIContainer textFontRow1 = new UIContainer(UIContainer.HORIZONTAL, UIContainer.CENTER);
        textFontRow1.zeroMargin().noOutline();
        textFontRow1.add(new UIDropdown(
                TextTool.FONT_NAMES,
                () -> 0,
                _ -> {
                }, true));
        textFontContainer.add(textFontRow1);
        textFontContainer.add(new UIText("Font"));
        textTools.add(textFontContainer);

        topRow.add(textTools);

        for (int i = 0; i < 2; i++) {
            final int index = i;
            UIContainer colorContainer = new UIContainer(UIContainer.VERTICAL, UIContainer.CENTER);
            setSelectableButtonStyle(colorContainer, () -> app.getColorSelection() == index);
            colorContainer.setSelectable(true);

            Supplier<Integer> cg = i == 0 ? app::getPrimaryColor : app::getSecondaryColor;
            colorContainer.add(new UIColorElement(cg, UISizes.BIG_COLOR_BUTTON));
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
            UIColorElement button = new UIColorElement(() -> color, UISizes.COLOR_BUTTON);
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

        addToRoot(topRow.addScrollbars().setHFillSize());

        addToRoot(new UISeparator().zeroMargin());

        UIContainer mainRow = new UIContainer(UIContainer.HORIZONTAL, UIContainer.TOP);
        mainRow.withSeparators(false).noBackground().noOutline();
        mainRow.setFillSize();

        mainRow.add(new ImageCanvas(UIContainer.VERTICAL, UIContainer.RIGHT, UIContainer.TOP, app));

        UIContainer sidePanel = new UIContainer(UIContainer.VERTICAL, UIContainer.CENTER, UIContainer.TOP,
                UIContainer.VERTICAL);
        sidePanel.withSeparators(true).setVFillSize().withBackground().noOutline();

        sidePanel.add(new ColorPickContainer(
                app.getSelectedColorPicker(),
                app::addCustomColor,
                UISizes.COLOR_PICKER_PANEL,
                UIContainer.VERTICAL,
                true,
                false));

        sidePanel.add(new UIRadioButtonList(
                UIContainer.VERTICAL,
                new String[] { "Option 1", "Option 2", "Option 3", "Option 4" },
                this::getTest,
                this::setTest));

        UIContainer debugPanel = new UIContainer(UIContainer.VERTICAL, UIContainer.LEFT, UIContainer.TOP,
                UIContainer.VERTICAL);
        debugPanel.setVFillSize().noOutline();
        debugPanel.setHFixedSize(400);

        debugPanel.add(new UIImage(0, new SVector(200, 200)) {
            @Override
            public void update() {
                super.update();
                textureID = app.getImage().getTextureID();
            };
        }.withOutline());

        debugPanel.add(new UIText("Tools"));
        for (ImageTool tool : ImageTool.INSTANCES) {
            debugPanel.add(new UIText(() -> String.format(" %s: state = %d", tool.getName(), tool.getState())));
        }
        debugPanel.add(new UIText(() -> String.format("Active tool: %s", app.getActiveTool().getName())));
        debugPanel.add(new UIText(() -> String.format(" State: %d", app.getActiveTool().getState())));
        debugPanel.add(new UIText(() -> String.format("TextTool.text: \"%s\"", ImageTool.TEXT.getText())));
        debugPanel.add(new UIText(() -> String.format("TextTool.font: \"%s\"", ImageTool.TEXT.getFont())));

        // debugPanel.add(new UIText(" "));
        // String[] lipsum = lipsum(Integer.MAX_VALUE, 3);
        // for (int i = 0; i < 20; i++) {
        // debugPanel.add(new UILabel(lipsum));
        // }

        // debugPanel.add(new UITextInput(this::getDebugString, this::setDebugString,
        // true));

        sidePanel.add(debugPanel.addScrollbars().setRelativeLayer(ImageCanvas.NUM_UI_LAYERS));

        mainRow.add(sidePanel.addScrollbars().setRelativeLayer(ImageCanvas.NUM_UI_LAYERS));

        root.add(mainRow);

        addToRoot(new UISeparator().zeroMargin());

        UIContainer statusBar = new UIContainer(UIContainer.HORIZONTAL, UIContainer.LEFT, UIContainer.CENTER);
        statusBar.withSeparators(false).withBackground().noOutline();
        statusBar.setHFillSize();
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
        }, true));
        statusBar.add(new UILabel(() -> {
            Image image = app.getImage();
            int width = image.getWidth();
            int height = image.getHeight();
            return "Size: %d x %d px".formatted(width, height);
        }, true));
        statusBar.add(new UILabel(() -> {
            int[] mouseImagePos = app.getMouseImagePosition();
            boolean inside = app.getImage().isInside(mouseImagePos[0], mouseImagePos[1]);
            String ret = "Mouse Position:";
            if (inside) {
                ret += " %d, %d".formatted(mouseImagePos[0], mouseImagePos[1]);
            }
            return ret;
        }, true));
        statusBar.add(new UILabel(() -> {
            String ret = "Selection size:";
            SelectionTool selection = ImageTool.SELECTION;
            if (app.getActiveTool() == selection
                    && selection.getState() != DragTool.NONE) {
                ret += " %d x %d px".formatted(ImageTool.SELECTION.getWidth(), ImageTool.SELECTION.getHeight());
            }
            return ret;
        }, true));
        statusBar.add(new UIContainer(0, 0).setHFillSize().noOutline());
        statusBar.add(new UILabel(() -> String.format("Frame %d", app.getFrameCount()), true));
        statusBar.add(new UILabel(() -> String.format("%.1f fps", app.getFrameRate()), true));

        addToRoot(statusBar);
    }

    private void addToRoot(UIElement element) {
        root.add(element.setRelativeLayer(ImageCanvas.NUM_UI_LAYERS));
    }

    private UIContainer createToggleContainer(String label, Supplier<Boolean> getter, Consumer<Boolean> setter) {
        UIContainer container = new UIContainer(UIContainer.HORIZONTAL, UIContainer.CENTER);
        container.zeroMargin().noOutline();
        container.setHFillSize();
        UIContainer fill = new UIContainer(0, 0);
        fill.noOutline();
        fill.setHFillSize();
        container.add(new UIText(label));
        container.add(fill);
        UIToggle toggle = new UIToggle(getter, setter);
        container.add(toggle);
        return container;
    }

    private UIContainer addTopRowSection(UIContainer topRow, String name) {
        return addTopRowSection(topRow, name, null);
    }

    private UIContainer addTopRowSection(UIContainer topRow, String name, Supplier<Boolean> visibilitySupplier) {
        UIContainer options = new UIContainer(UIContainer.VERTICAL, UIContainer.CENTER);
        options.zeroMargin().noOutline();
        options.setVFillSize();
        options.setPaddingScale(2);
        // options.zeroPadding();
        if (visibilitySupplier != null) {
            options.setVisibilitySupplier(visibilitySupplier);
        }

        UIContainer optionButtons = new UIContainer(UIContainer.HORIZONTAL, UIContainer.CENTER);
        optionButtons.zeroMargin().noOutline();
        options.add(optionButtons);

        // UIContainer fill = new UIContainer(0, 0);
        // fill.setVFillSize().zeroMargin().noOutline();
        // options.add(fill);

        options.add(new UIText(name, true));

        topRow.add(options);
        return optionButtons;
    }

    private UIContainer createIntPicker(Supplier<Integer> sizeGetter, Consumer<Integer> sizeSetter) {
        UIContainer container = new UIContainer(UIContainer.HORIZONTAL, UIContainer.CENTER);
        container.zeroMargin().zeroPadding().noOutline();
        container.add(new UIButton("-", () -> sizeSetter.accept(sizeGetter.get() - 1)));
        UINumberInput textSizeInput = new UINumberInput(sizeGetter, sizeSetter);
        textSizeInput.setVFillSize();
        textSizeInput.setHAlignment(UIContainer.CENTER);
        container.add(textSizeInput);
        container.add(new UIButton("+", () -> sizeSetter.accept(sizeGetter.get() + 1)));
        return container;
    }

    public String getDebugString() {
        return debugString;
    }

    public void setDebugString(String debugString) {
        this.debugString = debugString;
    }

    public int getTest() {
        return test;
    }

    public void setTest(int index) {
        this.test = index;
    }
}