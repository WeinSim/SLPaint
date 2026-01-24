package ui;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.lwjgl.glfw.GLFW;

import main.Image;
import main.ImageFormat;
import main.apps.MainApp;
import main.tools.DragTool;
import main.tools.ImageTool;
import main.tools.PencilTool;
import main.tools.TextTool;
import sutil.SUtil;
import sutil.math.SVector;
import sutil.ui.UIButton;
import sutil.ui.UIContainer;
import sutil.ui.UIDropdown;
import sutil.ui.UIFloatMenu;
import sutil.ui.UIImage;
import sutil.ui.UILabel;
import sutil.ui.UIMenuBar;
import sutil.ui.UINumberInput;
import sutil.ui.UIScale;
import sutil.ui.UISizes;
import sutil.ui.UIText;
import sutil.ui.UIToggle;
import ui.components.ColorPickContainer;
import ui.components.CustomColorContainer;
import ui.components.ImageCanvas;
import ui.components.UIColorElement;

public class MainUI extends AppUI<MainApp> {

    public static final int NUM_COLOR_BUTTONS_PER_ROW = 10;

    private String debugString = "";

    private int cursor = 0;

    public MainUI(MainApp app) {
        super(app);
    }

    @Override
    protected void init() {
        root.setOrientation(UIContainer.VERTICAL);
        root.setHAlignment(UIContainer.LEFT);
        root.withSeparators(false);

        UIMenuBar menuBar = new UIMenuBar();

        UIFloatMenu fileMenu = new UIFloatMenu();
        fileMenu.addLabel("New", app::newImage);
        fileMenu.addLabel("Open", app::openImage);
        fileMenu.addLabel("Save", app::saveImage);
        fileMenu.addLabel("Save As", app::saveImageAs);
        fileMenu.addSeparator();
        fileMenu.addLabel("Settings", () -> app.showDialog(MainApp.SETTINGS_DIALOG));
        fileMenu.addSeparator();
        fileMenu.addLabel("Quit", app::exit);
        menuBar.addMenu("File", fileMenu);

        UIFloatMenu editMenu = new UIFloatMenu();
        editMenu.addLabel("Undo", null);
        editMenu.addLabel("Redo", null);
        menuBar.addMenu("Edit", editMenu);

        UIFloatMenu selectionMenu = new UIFloatMenu();
        selectionMenu.addLabel("Copy", app::copySelection);
        selectionMenu.addLabel("Cut", app::cutSelection);
        selectionMenu.addLabel("Paste", app::pasteSelection);
        selectionMenu.addSeparator();
        selectionMenu.addLabel("Crop image to selection", app::cropImageToSelection);
        menuBar.addMenu("Selection", selectionMenu);

        UIFloatMenu imageMenu = new UIFloatMenu();
        imageMenu.addLabel("Resize", () -> app.showDialog(MainApp.RESIZE_DIALOG));
        imageMenu.addLabel("Crop", () -> app.showDialog(MainApp.CROP_DIALOG));
        menuBar.addMenu("Image", imageMenu);

        UIFloatMenu helpMenu = new UIFloatMenu();
        helpMenu.addLabel("About", () -> app.showDialog(MainApp.ABOUT_DIALOG));
        menuBar.addMenu("Help", helpMenu);

        root.add(menuBar);

        UIContainer topRow = new UIContainer(UIContainer.HORIZONTAL, UIContainer.LEFT, UIContainer.CENTER,
                UIContainer.HORIZONTAL);
        topRow.withSeparators(true).setHFillSize().setHAlignment(UIContainer.LEFT).withBackground().noOutline();

        UIContainer imageOptions = addTopRowSection(topRow, "Image");
        // imageOptions.add(new UILabel("..."));
        imageOptions.add(new UIButton("Resize", () -> app.showDialog(MainApp.RESIZE_DIALOG)));
        imageOptions.add(new UIButton("Rotate", () -> app.showDialog(MainApp.ROTATE_DIALOG)));
        imageOptions.add(new UIButton("Flip", () -> app.showDialog(MainApp.FLIP_DIALOG)));

        UIContainer toolbox = addTopRowSection(topRow, "Tools");
        for (ImageTool tool : ImageTool.INSTANCES) {
            UIButton toolButton = new UIButton(tool.getName().charAt(0) + "", () -> app.setActiveTool(tool));
            toolButton.setHandCursor();
            setSelectableButtonStyle(toolButton, () -> app.getActiveTool() == tool);
            toolbox.add(toolButton);
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

        UIContainer primSecColorContainer = new UIContainer(UIContainer.HORIZONTAL, UIContainer.TOP);
        primSecColorContainer.zeroMargin().setPaddingScale(1.0).noOutline();

        for (int i = 0; i < 2; i++) {
            final int index = i;
            UIContainer colorContainer = new UIContainer(UIContainer.VERTICAL, UIContainer.CENTER);
            // colorContainer.zeroMargin();
            setSelectableButtonStyle(colorContainer, () -> app.getColorSelection() == index);
            colorContainer.setSelectable(true);

            Supplier<Integer> cg = i == 0 ? app::getPrimaryColor : app::getSecondaryColor;
            colorContainer.add(new UIColorElement(cg, UISizes.BIG_COLOR_BUTTON));
            UILabel label = new UILabel(
                    "%s\nColor".formatted(i == 0 ? "Primary" : "Secondary")
            // "Color %d".formatted(i + 1)
            // i == 0 ? "Primary" : "Secondary"
            );
            label.setAlignment(UIContainer.CENTER);
            label.zeroMargin();
            colorContainer.add(label);

            colorContainer.setLeftClickAction(() -> app.setColorSelection(index));

            primSecColorContainer.add(colorContainer);
        }

        topRow.add(primSecColorContainer);

        final double colorPaddingScale = 1.0;

        UIContainer allColors = new UIContainer(UIContainer.VERTICAL, UIContainer.LEFT);
        allColors.zeroMargin().setPaddingScale(colorPaddingScale).noOutline();
        UIContainer currentRow = null;
        for (int i = 0; i < MainApp.DEFAULT_COLORS.length; i++) {
            if (currentRow == null) {
                currentRow = new UIContainer(UIContainer.HORIZONTAL, UIContainer.CENTER);
                currentRow.zeroMargin().setPaddingScale(colorPaddingScale).noOutline();
            }

            final int color = MainApp.DEFAULT_COLORS[i];
            UIColorElement button = new UIColorElement(() -> color, UISizes.COLOR_BUTTON);
            button.setLeftClickAction(() -> app.selectColor(color));
            button.setCursorShape(() -> button.mouseAbove() ? GLFW.GLFW_POINTING_HAND_CURSOR : null);
            currentRow.add(button);

            if ((i + 1) % NUM_COLOR_BUTTONS_PER_ROW == 0 || i + 1 == MainApp.DEFAULT_COLORS.length) {
                allColors.add(currentRow);
                currentRow = null;
            }
        }
        CustomColorContainer ccc = new CustomColorContainer(app.getCustomColorButtonArray(), app::selectColor);
        ccc.zeroMargin().setPaddingScale(colorPaddingScale).noOutline();
        allColors.add(ccc);
        topRow.add(allColors);

        root.add(topRow.addScrollbars().setHFillSize());

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
                // UISizes.COLOR_PICKER_PANEL,
                UIContainer.VERTICAL,
                true,
                false));

        UIContainer debugPanel = new UIContainer(UIContainer.VERTICAL, UIContainer.LEFT, UIContainer.TOP,
                UIContainer.VERTICAL);
        debugPanel.setFillSize().noOutline();

        UIImage debugImage = new UIImage(() -> app.getImage().getTextureID(), new SVector(200, 200));
        debugImage.withOutline();
        debugPanel.add(debugImage);

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

        // sidePanel.add(debugPanel.addScrollbars());

        mainRow.add(sidePanel.addScrollbars());

        root.add(mainRow);

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
        }, UIText.SMALL));
        statusBar.add(new UILabel(() -> {
            int width, height;
            if (app.isImageResizing()) {
                width = app.getNewImageWidth();
                height = app.getNewImageHeight();
            } else {
                Image image = app.getImage();
                width = image.getWidth();
                height = image.getHeight();
            }
            return "Image Size: %d x %d px".formatted(width, height);
        }, UIText.SMALL));
        statusBar.add(new UILabel(
                () -> String.format("Selection size: %d x %d px",
                        ImageTool.SELECTION.getWidth(),
                        ImageTool.SELECTION.getHeight()),
                UIText.SMALL).setVisibilitySupplier(
                        () -> ImageTool.SELECTION.getState() != DragTool.NONE));
        statusBar.add(new UILabel(() -> {
            int[] mouseImagePos = app.getMouseImagePosition();
            boolean inside = app.getImage().isInside(mouseImagePos[0], mouseImagePos[1]);
            String ret = "Mouse Position:";
            if (inside) {
                ret += " %d, %d".formatted(mouseImagePos[0], mouseImagePos[1]);
            }
            return ret;
        }, UIText.SMALL));
        statusBar.add(new UIContainer(0, 0).setHFillSize().noOutline());
        statusBar.add(new UILabel(() -> String.format("Frame %d", app.getFrameCount()), UIText.SMALL));
        statusBar.add(new UILabel(() -> String.format("%.1f fps", app.getFrameRate()), UIText.SMALL));

        root.add(statusBar);
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

        options.add(new UIText(name, UIText.SMALL));

        topRow.add(options);
        return optionButtons;
    }

    private UIContainer createIntPicker(Supplier<Integer> sizeGetter, Consumer<Integer> sizeSetter) {
        UIContainer container = new UIContainer(UIContainer.HORIZONTAL, UIContainer.CENTER);
        container.zeroMargin().zeroPadding().noOutline();
        UIButton minusButton = new UIButton("-", () -> sizeSetter.accept(sizeGetter.get() - 1));
        minusButton.setCursorShape(() -> minusButton.mouseAbove() ? GLFW.GLFW_POINTING_HAND_CURSOR : null);
        container.add(minusButton);
        UINumberInput textSizeInput = new UINumberInput(sizeGetter, sizeSetter);
        textSizeInput.setVFillSize();
        textSizeInput.setHAlignment(UIContainer.CENTER);
        container.add(textSizeInput);
        UIButton plusButton = new UIButton("+", () -> sizeSetter.accept(sizeGetter.get() + 1));
        plusButton.setCursorShape(() -> plusButton.mouseAbove() ? GLFW.GLFW_POINTING_HAND_CURSOR : null);
        container.add(plusButton);
        return container;
    }

    public String getDebugString() {
        return debugString;
    }

    public void setDebugString(String debugString) {
        this.debugString = debugString;
    }

    public int getCursor() {
        return cursor;
    }

    public void setCursor(int cursor) {
        this.cursor = cursor;
    }
}