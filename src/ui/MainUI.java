package ui;

import static org.lwjgl.glfw.GLFW.*;

import java.util.function.BooleanSupplier;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import org.lwjglx.util.vector.Vector4f;

import main.apps.MainApp;
import main.image.Image;
import main.image.ImageFormat;
import main.tools.DragTool;
import main.tools.ImageTool;
import main.tools.LineTool;
import main.tools.PencilTool;
import main.tools.SelectionTool;
import main.tools.TextTool;
import sutil.SUtil;
import sutil.ui.UI;
import sutil.ui.UISizes;
import sutil.ui.elements.UIButton;
import sutil.ui.elements.UIContainer;
import sutil.ui.elements.UIDropdown;
import sutil.ui.elements.UIElement;
import sutil.ui.elements.UIFloatMenu;
import sutil.ui.elements.UILabel;
import sutil.ui.elements.UIMenuBar;
import sutil.ui.elements.UINumberInput;
import sutil.ui.elements.UIScale;
import sutil.ui.elements.UIText;
import sutil.ui.elements.UIToggleList;
import ui.components.ColorPickContainer;
import ui.components.CustomColorContainer;
import ui.components.ImageCanvas;
import ui.components.ToolButton;
import ui.components.UIColorElement;

public class MainUI extends AppUI<MainApp> {

    public static final int NUM_COLOR_BUTTONS_PER_ROW = 10;

    private String debugString = "";

    // private int test = 0;

    public MainUI(MainApp app) {
        super(app);
    }

    @Override
    protected void createKeyboardShortcuts() {
        super.createKeyboardShortcuts();

        // general keyboard shortcuts
        UI.addKeyboardShortcut("new", GLFW_KEY_N, GLFW_MOD_CONTROL, true, app::newImage);
        UI.addKeyboardShortcut("open", GLFW_KEY_O, GLFW_MOD_CONTROL, true, app::openImage);
        UI.addKeyboardShortcut("save", GLFW_KEY_S, GLFW_MOD_CONTROL, true, app::saveImage);
        UI.addKeyboardShortcut("save_as", GLFW_KEY_S, GLFW_MOD_CONTROL | GLFW_MOD_SHIFT, true, app::saveImageAs);
        UI.addKeyboardShortcut("undo", GLFW_KEY_Z, GLFW_MOD_CONTROL, app::canUndo, app::undo);
        UI.addKeyboardShortcut("redo", GLFW_KEY_Y, GLFW_MOD_CONTROL, app::canRedo, app::redo);
        UI.addKeyboardShortcut("reset_transform", GLFW_KEY_R, 0, false, app::resetImageTransform);
        UI.addKeyboardShortcut("zoom_in", GLFW_KEY_KP_ADD, GLFW_MOD_CONTROL, app::canZoomIn, app::zoomIn);
        UI.addKeyboardShortcut("zoom_out", GLFW_KEY_KP_SUBTRACT, GLFW_MOD_CONTROL, app::canZoomOut, app::zoomOut);
        UI.addKeyboardShortcut("reset_zoom", GLFW_KEY_0, GLFW_MOD_CONTROL, true, app::resetZoom);

        // all tool shortcuts
        for (ImageTool tool : ImageTool.INSTANCES) {
            tool.setApp(app);
            tool.createKeyboardShortcuts();
        }
    }

    @Override
    protected void init() {
        root.setOrientation(VERTICAL);
        root.setHAlignment(LEFT);
        root.withSeparators(false);

        UIMenuBar menuBar = new UIMenuBar();

        UIFloatMenu fileMenu = menuBar.addMenu("File");
        fileMenu.addLabel("New", getKeyboardShortcut("new"));
        fileMenu.addLabel("Open", getKeyboardShortcut("open"));
        fileMenu.addLabel("Save", getKeyboardShortcut("save"));
        fileMenu.addLabel("Save As", getKeyboardShortcut("save_as"));
        fileMenu.addSeparator();
        fileMenu.addLabel("Settings", () -> app.showDialog(MainApp.SETTINGS_DIALOG));
        fileMenu.addSeparator();
        fileMenu.addLabel("Quit", app::requestClose);

        UIFloatMenu editMenu = menuBar.addMenu("Edit");
        editMenu.addLabel("Undo", getKeyboardShortcut("undo"));
        editMenu.addLabel("Redo", getKeyboardShortcut("redo"));

        UIFloatMenu selectionMenu = menuBar.addMenu("Selection");
        selectionMenu.addLabel("Select everything", getKeyboardShortcut("select_all"));
        selectionMenu.addSeparator();
        selectionMenu.addLabel("Copy", getKeyboardShortcut("copy"));
        selectionMenu.addLabel("Cut", getKeyboardShortcut("cut"));
        selectionMenu.addLabel("Paste", getKeyboardShortcut("paste"));
        selectionMenu.addSeparator();
        selectionMenu.addLabel("Crop image to selection", getKeyboardShortcut("crop_to_selection"));
        selectionMenu.addSeparator();
        UIFloatMenu selectionRotateMenu = selectionMenu.addNestedMenu("Rotate");
        final SelectionTool selection = ImageTool.SELECTION;
        final BooleanSupplier selectionActive = () -> selection.getState() == SelectionTool.IDLE;
        selectionRotateMenu.addLabel("Rotate 90° right", selection::rotateRight, selectionActive);
        selectionRotateMenu.addLabel("Rotate 90° left", selection::rotateLeft, selectionActive);
        selectionRotateMenu.addLabel("Rotate 180°", selection::rotate180, selectionActive);
        UIFloatMenu selectionFlipMenu = selectionMenu.addNestedMenu("Flip");
        selectionFlipMenu.addLabel("Flip horizontally", selection::flipHorizontal, selectionActive);
        selectionFlipMenu.addLabel("Flip vertically", selection::flipVertical, selectionActive);

        UIFloatMenu viewMenu = menuBar.addMenu("View");
        viewMenu.addLabel("Zoom in", getKeyboardShortcut("zoom_in"));
        viewMenu.addLabel("Zoom out", getKeyboardShortcut("zoom_out"));
        viewMenu.addLabel("Reset zoom", getKeyboardShortcut("reset_zoom"));
        viewMenu.addLabel("Reset view", getKeyboardShortcut("reset_transform"));

        UIFloatMenu imageMenu = menuBar.addMenu("Image");
        imageMenu.addLabel("Resize", () -> app.showDialog(MainApp.RESIZE_DIALOG));
        imageMenu.addLabel("Crop", () -> app.showDialog(MainApp.CROP_DIALOG));
        imageMenu.addSeparator();
        UIFloatMenu imageRotateMenu = imageMenu.addNestedMenu("Rotate");
        imageRotateMenu.addLabel("Rotate 90° right", app::rotateImageRight);
        imageRotateMenu.addLabel("Rotate 90° left", app::rotateImageLeft);
        imageRotateMenu.addLabel("Rotate 180°", app::rotateImage180);
        UIFloatMenu imageFlipMenu = imageMenu.addNestedMenu("Flip");
        imageFlipMenu.addLabel("Flip horizontally", app::flipImageHorizontal);
        imageFlipMenu.addLabel("Flip vertically", app::flipImageVertical);

        if (MainApp.DEV_BUILD) {
            UIFloatMenu debugMenu = menuBar.addMenu("Debug");
            debugMenu.addLabel("Toggle element outline", getKeyboardShortcut("cycle_debug"));
            debugMenu.addLabel("Reload shaders", getKeyboardShortcut("reload_shaders"));
            debugMenu.addLabel("Reload UI", getKeyboardShortcut("reload_ui"));
        }

        UIFloatMenu helpMenu = menuBar.addMenu("Help");
        helpMenu.addLabel("About", () -> app.showDialog(MainApp.ABOUT_DIALOG));

        root.add(menuBar);

        UIContainer mainRow = new UIContainer(HORIZONTAL, TOP);
        mainRow.withSeparators(false).noBackground().noOutline();
        mainRow.setFillSize();

        UIContainer sidePanel = new UIContainer(VERTICAL, CENTER, TOP,
                VERTICAL);
        sidePanel.withSeparators(true).setVFillSize().withBackground().noOutline();

        UIContainer primSecColorContainer = new UIContainer(HORIZONTAL, TOP);
        primSecColorContainer.zeroMargin().setPaddingScale(1.0).noOutline();

        for (int i = 0; i < 2; i++) {
            final int index = i;
            UIContainer colorContainer = new UIContainer(VERTICAL, CENTER);
            // colorContainer.zeroMargin();
            setSelectableButtonStyle(colorContainer, () -> app.getColorSelection() == index);
            colorContainer.setSelectable(true);

            Supplier<Vector4f> cg = i == 0
                    ? () -> MainApp.toVector4f(app.getPrimaryColor())
                    : () -> MainApp.toVector4f(app.getSecondaryColor());
            colorContainer.add(new UIColorElement(cg, UISizes.BIG_COLOR_BUTTON));
            UILabel label = new UILabel(String.format("%s\nColor", i == 0 ? "Primary" : "Secondary"),
                    UISizes.TEXT_SMALL);
            label.setAlignment(CENTER);
            label.zeroMargin();
            colorContainer.add(label);

            colorContainer.addLeftClickAction(() -> app.setColorSelection(index));

            primSecColorContainer.add(colorContainer);
        }

        sidePanel.add(primSecColorContainer);

        final double colorPaddingScale = 1.0;

        UIContainer allColors = new UIContainer(VERTICAL, LEFT);
        allColors.zeroMargin().setPaddingScale(colorPaddingScale).noOutline();
        UIContainer currentRow = null;
        for (int i = 0; i < MainApp.DEFAULT_COLORS.length; i++) {
            if (currentRow == null) {
                currentRow = new UIContainer(HORIZONTAL, CENTER);
                currentRow.zeroMargin().setPaddingScale(colorPaddingScale).noOutline();
            }

            final int colorInt = MainApp.DEFAULT_COLORS[i];
            final Vector4f color = MainApp.toVector4f(colorInt);
            UIColorElement button = new UIColorElement(() -> color, UISizes.COLOR_BUTTON);
            button.addLeftClickAction(() -> app.selectColor(colorInt));
            button.setCursorShape(() -> button.mouseAbove() ? GLFW_POINTING_HAND_CURSOR : null);
            currentRow.add(button);

            if ((i + 1) % NUM_COLOR_BUTTONS_PER_ROW == 0 || i + 1 == MainApp.DEFAULT_COLORS.length) {
                allColors.add(currentRow);
                currentRow = null;
            }
        }
        CustomColorContainer ccc = new CustomColorContainer(HORIZONTAL,
                app.getCustomColorButtonArray(),
                c -> app.selectColor(MainApp.toInt(c)));
        ccc.zeroMargin().setPaddingScale(colorPaddingScale).noOutline();
        allColors.add(ccc);
        sidePanel.add(allColors);

        sidePanel.add(new ColorPickContainer(
                app.getSelectedColorPicker(),
                app::addCustomColor,
                VERTICAL,
                true,
                false));

        UIContainer debugPanel = new UIContainer(VERTICAL, LEFT, TOP,
                VERTICAL);
        debugPanel.setFillSize().noOutline();

        // UIImage debugImage = new UIImage(() -> app.getImage().getTextureID(), new
        // SVector(200, 200));
        // debugImage.withOutline();
        // debugPanel.add(debugImage);

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

        UIContainer mainArea = new UIContainer(VERTICAL, TOP);
        mainArea.withSeparators(false).noOutline();
        mainArea.setFillSize();

        UIContainer toolRow = new UIContainer(HORIZONTAL, LEFT, CENTER, HORIZONTAL);
        toolRow.withSeparators(true).setHFillSize().setHAlignment(LEFT).withBackground().noOutline();

        UIContainer imageOptions = addTopRowSection(toolRow, "Image");
        imageOptions.add(new UIButton("Resize", () -> app.showDialog(MainApp.RESIZE_DIALOG)));
        UIDropdown rotateImage = new UIDropdown("Rotate");
        rotateImage.addLabel("Rotate 90° right", app::rotateImageRight);
        rotateImage.addLabel("Rotate 90° left", app::rotateImageLeft);
        rotateImage.addLabel("Rotate 180°", app::rotateImage180);
        imageOptions.add(rotateImage);
        UIDropdown flipImage = new UIDropdown("Flip");
        flipImage.addLabel("Flip horizontally", app::flipImageHorizontal);
        flipImage.addLabel("Flip vertically", app::flipImageVertical);
        imageOptions.add(flipImage);

        UIContainer toolbox = addTopRowSection(toolRow, "Tools");
        toolbox.setOrientation(VERTICAL);
        final int toolsPerRow = 6;
        UIContainer currentToolRow = null;
        for (int i = 0; i < ImageTool.INSTANCES.length; i++) {
            if (i % toolsPerRow == 0) {
                currentToolRow = new UIContainer(HORIZONTAL, CENTER);
                currentToolRow.zeroMargin().noOutline();
            }
            currentToolRow.add(new ToolButton(app, ImageTool.INSTANCES[i]));
            if ((i + 1) % toolsPerRow == 0 || i == ImageTool.INSTANCES.length - 1)
                toolbox.add(currentToolRow);
        }

        // Used for pencil size and line size
        UIContainer sizeTools = new UIContainer(VERTICAL, CENTER);
        sizeTools.zeroMargin().noOutline();
        sizeTools.setVisibilitySupplier(() -> switch (app.getActiveTool()) {
            case PencilTool _,LineTool _ -> true;
            default -> false;
        });

        IntSupplier sizeSupplier = () -> switch (app.getActiveTool()) {
            case PencilTool _ -> ImageTool.PENCIL.getSize();
            case LineTool _ -> ImageTool.LINE.getSize();
            default -> 0;
        };
        IntConsumer sizeConsumer = i -> {
            switch (app.getActiveTool()) {
                case PencilTool _ -> ImageTool.PENCIL.setSize(i);
                case LineTool _ -> ImageTool.LINE.setSize(i);
                default -> {
                }
            }
        };

        final double min = PencilTool.MIN_SIZE,
                max = PencilTool.MAX_SIZE;
        UIScale sizeScale = new UIScale(HORIZONTAL,
                () -> SUtil.map(sizeSupplier.getAsInt(), min, max, 0, 1),
                x -> sizeConsumer.accept((int) Math.round(SUtil.map(x, 0, 1, min, max))));
        sizeTools.add(sizeScale);

        UIContainer sizeBottomRow = new UIContainer(HORIZONTAL,
                CENTER);
        sizeBottomRow.zeroMargin().noOutline();
        sizeBottomRow.add(new UIText("Size:", UIText.SMALL));
        sizeBottomRow.add(new UIContainer(0, 0).setHFillSize().noOutline());
        sizeBottomRow.add(createIntPicker(sizeSupplier, sizeConsumer));

        sizeTools.add(sizeBottomRow);
        toolRow.add(sizeTools);

        UIContainer selectionTools = new UIContainer(VERTICAL, CENTER);
        selectionTools.zeroMargin().setPaddingScale(2.0).noOutline();
        selectionTools.setVisibilitySupplier(() -> app.getActiveTool() == ImageTool.SELECTION);
        UIContainer selectionToolsTop = new UIContainer(HORIZONTAL, CENTER);
        selectionToolsTop.zeroMargin().setPaddingScale(2.0).noOutline();

        UIContainer selectionButtons = new UIContainer(HORIZONTAL, LEFT);
        selectionButtons.zeroMargin().noOutline();
        UIDropdown rotateSelection = new UIDropdown("Rotate");
        // rotateSelection.setHFillSize();
        rotateSelection.addLabel("Rotate 90° right", selection::rotateRight);
        rotateSelection.addLabel("Rotate 90° left", selection::rotateLeft);
        rotateSelection.addLabel("Rotate 180°", selection::rotate180);
        selectionButtons.add(rotateSelection);
        UIDropdown flipSelection = new UIDropdown("Flip");
        // flipSelection.setHFillSize();
        flipSelection.addLabel("Flip horizontally", selection::flipHorizontal);
        flipSelection.addLabel("Flip vertically", selection::flipVertical);
        selectionButtons.add(flipSelection);
        selectionToolsTop.add(selectionButtons);

        UIToggleList selectionToggles = new UIToggleList();
        selectionToggles.setOrientation(HORIZONTAL).setPaddingScale(2.0);
        selectionToggles.addToggle("Transparent selection",
                MainApp::isTransparentSelection,
                MainApp::setTransparentSelection);
        selectionToggles.addToggle("Lock aspect ratio",
                MainApp::isLockSelectionRatio,
                MainApp::setLockSelectionRatio);
        selectionToolsTop.add(selectionToggles);

        selectionTools.add(selectionToolsTop);
        selectionTools.add(new UIText("Selection", UISizes.TEXT_SMALL));
        toolRow.add(selectionTools);

        UIContainer textTools = new UIContainer(HORIZONTAL, CENTER);
        textTools.zeroMargin().setVFillSize().noOutline();
        textTools.setVisibilitySupplier(() -> app.getActiveTool() == ImageTool.TEXT);
        textTools.setPaddingScale(2);

        UIContainer textSizeContainer = new UIContainer(VERTICAL, CENTER);
        textSizeContainer.zeroMargin().setPaddingScale(2).setVFillSize().noOutline();
        textSizeContainer.add(createIntPicker(ImageTool.TEXT::getSize, ImageTool.TEXT::setSize));
        textSizeContainer.add(new UIText("Size", UISizes.TEXT_SMALL));
        textTools.add(textSizeContainer);

        UIContainer textFontContainer = new UIContainer(VERTICAL, CENTER);
        textFontContainer.zeroMargin().setPaddingScale(2).setVFillSize().noOutline();
        UIContainer textFontRow1 = new UIContainer(HORIZONTAL, CENTER);
        textFontRow1.zeroMargin().noOutline();
        textFontRow1.add(new UIDropdown(
                TextTool.FONT_NAMES,
                () -> 0,
                _ -> {
                }, true));
        textFontContainer.add(textFontRow1);
        textFontContainer.add(new UIText("Font", UISizes.TEXT_SMALL));
        textTools.add(textFontContainer);

        toolRow.add(textTools);

        // topRow.add(new UIContainer(0, 0).setHFillSize().noOutline());

        mainArea.add(toolRow.addScrollbars().setHFillSize());

        mainArea.add(new ImageCanvas(VERTICAL, RIGHT, TOP, app));

        mainRow.add(mainArea);

        root.add(mainRow);

        UIContainer statusBar = new UIContainer(HORIZONTAL, LEFT, CENTER);
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
            if (filename != null && filename.length() > 0) {
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
        statusBar.add(new UILabel(() -> String.format("%d UI elements", countUIElements(UI.getRoot())), UIText.SMALL));
        statusBar.add(new UILabel(() -> String.format("%5.3f ms update", app.getAvgUpdateTime() / 1e-3), UIText.SMALL));
        statusBar.add(new UILabel(() -> String.format("%4.1f fps", app.getFrameRate()), UIText.SMALL));
        statusBar.add(new UILabel(() -> String.format("Frame %5d", app.getFrameCount()), UIText.SMALL));

        root.add(statusBar);
    }

    private UIContainer addTopRowSection(UIContainer topRow, String name) {
        return addTopRowSection(topRow, name, null);
    }

    private UIContainer addTopRowSection(UIContainer topRow, String name, BooleanSupplier visibilitySupplier) {
        UIContainer options = new UIContainer(VERTICAL, CENTER);
        options.zeroMargin().noOutline();
        options.setVFillSize();
        options.setPaddingScale(2);
        // options.zeroPadding();
        if (visibilitySupplier != null) {
            options.setVisibilitySupplier(visibilitySupplier);
        }

        UIContainer optionButtons = new UIContainer(HORIZONTAL, CENTER);
        optionButtons.zeroMargin().noOutline();
        options.add(optionButtons);

        // UIContainer fill = new UIContainer(0, 0);
        // fill.setVFillSize().zeroMargin().noOutline();
        // options.add(fill);

        options.add(new UIText(name, UIText.SMALL));

        topRow.add(options);
        return optionButtons;
    }

    private UIContainer createIntPicker(IntSupplier sizeGetter, IntConsumer sizeSetter) {
        UIContainer container = new UIContainer(HORIZONTAL, CENTER);
        container.zeroMargin().zeroPadding().noOutline();
        UIButton minusButton = new UIButton("-", () -> sizeSetter.accept(sizeGetter.getAsInt() - 1));
        minusButton.setCursorShape(() -> minusButton.mouseAbove() ? GLFW_POINTING_HAND_CURSOR : null);
        container.add(minusButton);
        UINumberInput textSizeInput = new UINumberInput(sizeGetter, sizeSetter);
        textSizeInput.setVFillSize();
        textSizeInput.setHAlignment(CENTER);
        container.add(textSizeInput);
        UIButton plusButton = new UIButton("+", () -> sizeSetter.accept(sizeGetter.getAsInt() + 1));
        plusButton.setCursorShape(() -> plusButton.mouseAbove() ? GLFW_POINTING_HAND_CURSOR : null);
        container.add(plusButton);
        return container;
    }

    public String getDebugString() {
        return debugString;
    }

    public void setDebugString(String debugString) {
        this.debugString = debugString;
    }

    private int countUIElements(UIElement element) {
        int sum = 1;
        if (element instanceof UIContainer container) {
            for (UIElement child : container.getChildren())
                sum += countUIElements(child);
        }
        return sum;
    }

    // public int getTest() {
    // return test;
    // }

    // public void setTest(int cursor) {
    // this.test = cursor;
    // }
}