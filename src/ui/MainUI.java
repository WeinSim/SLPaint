package ui;

import java.util.function.BooleanSupplier;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import org.lwjgl.glfw.GLFW;
import org.lwjglx.util.vector.Vector4f;

import main.Image;
import main.ImageFormat;
import main.apps.MainApp;
import main.tools.DragTool;
import main.tools.ImageTool;
import main.tools.LineTool;
import main.tools.PencilTool;
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
import sutil.ui.elements.UIMenuButton;
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
    protected void init() {
        root.setOrientation(VERTICAL);
        root.setHAlignment(LEFT);
        root.withSeparators(false);

        UIMenuBar menuBar = new UIMenuBar();

        UIFloatMenu fileMenu = menuBar.addMenu("File");
        fileMenu.addLabel("New", app.getKeyboardShortcut("new"));
        fileMenu.addLabel("Open", app.getKeyboardShortcut("open"));
        fileMenu.addLabel("Save", app.getKeyboardShortcut("save"));
        fileMenu.addLabel("Save As", app.getKeyboardShortcut("save_as"));
        fileMenu.addSeparator();
        fileMenu.addLabel("Settings", () -> app.showDialog(MainApp.SETTINGS_DIALOG));
        fileMenu.addSeparator();
        fileMenu.addLabel("Quit", app::exit);

        UIFloatMenu editMenu = menuBar.addMenu("Edit");
        editMenu.addLabel("Undo", app.getKeyboardShortcut("undo"));
        editMenu.addLabel("Redo", app.getKeyboardShortcut("redo"));

        UIFloatMenu selectionMenu = menuBar.addMenu("Selection");
        selectionMenu.addLabel("Select everything", app.getKeyboardShortcut("select_all"));
        selectionMenu.addSeparator();
        selectionMenu.addLabel("Copy", app.getKeyboardShortcut("copy"));
        selectionMenu.addLabel("Cut", app.getKeyboardShortcut("cut"));
        selectionMenu.addLabel("Paste", app.getKeyboardShortcut("paste"));
        selectionMenu.addSeparator();
        selectionMenu.addLabel("Crop image to selection", app.getKeyboardShortcut("crop_to_selection"));

        UIFloatMenu viewMenu = menuBar.addMenu("View");
        viewMenu.addLabel("Zoom in", app.getKeyboardShortcut("zoom_in"));
        viewMenu.addLabel("Zoom out", app.getKeyboardShortcut("zoom_out"));
        viewMenu.addLabel("Reset zoom", app.getKeyboardShortcut("reset_zoom"));
        viewMenu.addSeparator();
        viewMenu.addLabel("Reset view", app.getKeyboardShortcut("reset_transform"));

        UIFloatMenu imageMenu = menuBar.addMenu("Image");
        imageMenu.addLabel("Resize", () -> app.showDialog(MainApp.RESIZE_DIALOG));
        imageMenu.addLabel("Crop", () -> app.showDialog(MainApp.CROP_DIALOG));
        imageMenu.addSeparator();
        UIFloatMenu rotateMenu = imageMenu.addNestedMenu("Rotate");
        rotateMenu.addLabel("Rotate 90° right", () -> app.rotateImage(90));
        rotateMenu.addLabel("Rotate 90° left", () -> app.rotateImage(-90));
        rotateMenu.addLabel("Rotate 180°", () -> app.rotateImage(180));
        UIFloatMenu flipMenu = imageMenu.addNestedMenu("Flip");
        flipMenu.addLabel("Flip horizontally", () -> app.flipImageHorizontal());
        flipMenu.addLabel("Flip vertically", () -> app.flipImageVertical());

        UIFloatMenu helpMenu = menuBar.addMenu("Help");
        helpMenu.addLabel("About", () -> app.showDialog(MainApp.ABOUT_DIALOG));

        root.add(menuBar);

        UIContainer topRow = new UIContainer(HORIZONTAL, LEFT, CENTER, HORIZONTAL);
        topRow.withSeparators(true).setHFillSize().setHAlignment(LEFT).withBackground().noOutline();

        UIContainer imageOptions = addTopRowSection(topRow, "Image");
        imageOptions.add(new UIButton("Resize", () -> app.showDialog(MainApp.RESIZE_DIALOG)));
        UIMenuButton rotate = new UIMenuButton("Rotate");
        rotate.addLabel("Rotate 90° right", () -> app.rotateImage(90));
        rotate.addLabel("Rotate 90° left", () -> app.rotateImage(-90));
        rotate.addLabel("Rotate 180°", () -> app.rotateImage(180));
        imageOptions.add(rotate);
        UIMenuButton flip = new UIMenuButton("Flip");
        flip.addLabel("Flip horizontally", () -> app.flipImageHorizontal());
        flip.addLabel("Flip vertically", () -> app.flipImageVertical());
        imageOptions.add(flip);

        UIContainer toolbox = addTopRowSection(topRow, "Tools");
        toolbox.setOrientation(VERTICAL);
        final int toolsPerRow = 3;
        UIContainer toolRow = null;
        for (int i = 0; i < ImageTool.INSTANCES.length; i++) {
            if (i % toolsPerRow == 0) {
                toolRow = new UIContainer(HORIZONTAL, CENTER);
                toolRow.zeroMargin().noOutline();
            }
            toolRow.add(new ToolButton(app, ImageTool.INSTANCES[i]));
            if ((i + 1) % toolsPerRow == 0 || i == ImageTool.INSTANCES.length - 1)
                toolbox.add(toolRow);
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
        topRow.add(sizeTools);

        UIToggleList selectionTools = new UIToggleList();
        selectionTools.setVisibilitySupplier(() -> app.getActiveTool() == ImageTool.SELECTION);

        selectionTools.addToggle("Transparent selection",
                MainApp::isTransparentSelection,
                MainApp::setTransparentSelection);

        selectionTools.addToggle("Lock selection ratio",
                MainApp::isLockSelectionRatio,
                MainApp::setLockSelectionRatio);

        topRow.add(selectionTools);

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

        topRow.add(textTools);

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
            UILabel label = new UILabel(
                    "%s\nColor".formatted(i == 0 ? "Primary" : "Secondary")
            // "Color %d".formatted(i + 1)
            // i == 0 ? "Primary" : "Secondary"
            );
            label.setAlignment(CENTER);
            label.zeroMargin();
            colorContainer.add(label);

            colorContainer.setLeftClickAction(() -> app.setColorSelection(index));

            primSecColorContainer.add(colorContainer);
        }

        topRow.add(primSecColorContainer);

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
            button.setLeftClickAction(() -> app.selectColor(colorInt));
            button.setCursorShape(() -> button.mouseAbove() ? GLFW.GLFW_POINTING_HAND_CURSOR : null);
            currentRow.add(button);

            if ((i + 1) % NUM_COLOR_BUTTONS_PER_ROW == 0 || i + 1 == MainApp.DEFAULT_COLORS.length) {
                allColors.add(currentRow);
                currentRow = null;
            }
        }
        CustomColorContainer ccc = new CustomColorContainer(app.getCustomColorButtonArray(),
                c -> app.selectColor(MainApp.toInt(c)));
        ccc.zeroMargin().setPaddingScale(colorPaddingScale).noOutline();
        allColors.add(ccc);
        topRow.add(allColors);

        root.add(topRow.addScrollbars().setHFillSize());

        UIContainer mainRow = new UIContainer(HORIZONTAL, TOP);
        mainRow.withSeparators(false).noBackground().noOutline();
        mainRow.setFillSize();

        mainRow.add(new ImageCanvas(VERTICAL, RIGHT, TOP, app));

        UIContainer sidePanel = new UIContainer(VERTICAL, CENTER, TOP,
                VERTICAL);
        sidePanel.withSeparators(true).setVFillSize().withBackground().noOutline();

        sidePanel.add(new ColorPickContainer(
                app.getSelectedColorPicker(),
                app::addCustomColor,
                // UISizes.COLOR_PICKER_PANEL,
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
        minusButton.setCursorShape(() -> minusButton.mouseAbove() ? GLFW.GLFW_POINTING_HAND_CURSOR : null);
        container.add(minusButton);
        UINumberInput textSizeInput = new UINumberInput(sizeGetter, sizeSetter);
        textSizeInput.setVFillSize();
        textSizeInput.setHAlignment(CENTER);
        container.add(textSizeInput);
        UIButton plusButton = new UIButton("+", () -> sizeSetter.accept(sizeGetter.getAsInt() + 1));
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