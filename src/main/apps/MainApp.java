package main.apps;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.lwjgl.glfw.GLFW;

import main.ClipboardManager;
import main.ColorArray;
import main.ColorPicker;
import main.Image;
import main.ImageFile;
import main.ImageFileManager;
import main.ImageFormat;
import main.ImageTool;
import main.SelectionManager;
import main.dialogs.SaveDialog;
import main.dialogs.UnableToSaveImageDialog;
import main.dialogs.UnimplementedDialog;
import main.settings.BooleanSetting;
import main.settings.ColorArraySetting;
import main.settings.Settings;
import renderEngine.MainAppRenderer;
import renderEngine.Window;
import sutil.SUtil;
import sutil.math.SVector;
import ui.MainUI;
import ui.Sizes;
import ui.components.ImageCanvas;

/**
 * TODO:
 * Move todo's scattered across different files into this central todo list
 * App:
 * * Dialogs
 * * * Save dialog
 * * * * Keep track of unsaved changes, ask user to save before quitting if
 * * * * * there are unsaved changes
 * * * Only one at a time
 * * * Keep track of all file locks in one centralized place to avoid leaking
 * * Resizing
 * * * Selection resizing
 * * * Selection Ctrl+Shift+X
 * * Save user settings (ui base color, light / dark mode, transparent
 * * * selection)
 * * Undo / redo
 * * Pencil
 * * * Add different sizes
 * * * Fix bug: pencil draws when mouse is clicked outside of canvas and dragged
 * * * * onto canvas
 * * Text tool
 * * Line tool
 * * Transparency
 * * * Grey-white squares should always appear the same size
 * * * Simply selecting a transparent area and unselecting it causes the
 * * * * transparency to go away. Reason: selecting the area replaces that part
 * * * * of the image with the secondary color. Placing the transparent
 * * * * selection back onto the opaque background leaves the background
 * * * * unaffected. What is the expected behavior here?
 * * Recognize remapping from CAPS_LOCK to ESCAPE
 * * When parent app closes, shouldren should also close
 * UI:
 * * Scrolling
 * * Tool icons & cursors
 * * UIFloat element (e.g. dropdown menues): doesn't affect parent's size
 * * (Add hMargin and vMargin in UIContainer)
 * * * Not neccessary for now. UISeparators now extend fully even without
 * * * hMargin and vMargin
 * * HueSatField's hitbox should adjust (rect / circle) depending on the setting
 * * Fix small bug in UILabel (see {@link sutil.ui.UILabel#textUpdater})
 * * In ColorPickContainer hide either HSL or HSV input (depending on the user
 * * * setting. (do after pulling ui_layers because of UIElement.visible
 * * * stautus)
 * Rendering:
 * * Weird rendering bugs:
 * * * Anti aliasing doesn't work despite being enabled
 * * * * (glfwWindowHint(GLFW_SAMPLES, 4) and glEnable(GL_MULTISAMPLE))
 * * * Selection border sometimes has artifacts on bottom and right inner edges
 * * * AlphaScale has artifacts on bottom edge (whose size depends on wether a
 * * * * text cursor is currently visible?!?)
 * * Fix stuttering artifact when resizing windows on Linux
 * * * (see https://www.glfw.org/docs/latest/window.html#window_refresh)
 * * Clean up UIRenderMaster API and UI shaders (especially with regards to
 * * * transparency!)
 * * Premultiply view matrix and transformation matrix (for rect and text
 * * * shader)
 * * Rename transformationMatrix to uiMatrix
 * * Remove magic numbers in {@link renderEngine.MainAppRenderer#render()}
 * * "Activate alpha blending" in
 * * * {@link renderEngine.UIRenderMaster#image(int, SVector, SVector)}
 * * * (whatever that means??)
 * Maximized windows don't show up correctly on Windows 11
 * Error handling
 * 3D UI view?
 */
public final class MainApp extends App {

    public static final int RGB_BITMASK = 0x00FFFFFF;

    public static final int[] DEFAULT_COLORS = {
            SUtil.toARGB(0),
            SUtil.toARGB(63),
            SUtil.toARGB(132, 15, 24),
            SUtil.toARGB(230, 46, 46),
            SUtil.toARGB(249, 131, 57),
            SUtil.toARGB(255, 242, 65),
            SUtil.toARGB(61, 176, 85),
            SUtil.toARGB(39, 161, 228),
            SUtil.toARGB(62, 73, 199),
            SUtil.toARGB(159, 77, 161),
            SUtil.toARGB(255),
            SUtil.toARGB(127),
            SUtil.toARGB(182, 123, 91),
            SUtil.toARGB(250, 176, 201),
            SUtil.toARGB(253, 202, 59),
            SUtil.toARGB(239, 229, 180),
            SUtil.toARGB(186, 229, 64),
            SUtil.toARGB(159, 216, 235),
            SUtil.toARGB(114, 146, 187),
            SUtil.toARGB(199, 191, 230),
    };

    public static final int SAVE_DIALOG = 1, NEW_DIALOG = 2, CHANGE_SIZE_DIALOG = 3, NEW_COLOR_DIALOG = 4,
            ROTATE_DIALOG = 5, FLIP_DIALOG = 6, UNABLE_TO_SAVE_IMAGE_DIALOG = 7, DISCARD_UNSAVED_CHANGES_DIALOG = 9,
            SETTINGS_DIALOG = 10;

    public static final int PRIMARY_COLOR = 0, SECONDARY_COLOR = 1;

    private static final int MIN_ZOOM_LEVEL = -4;
    private static final int MAX_ZOOM_LEVEL = 8;
    private static final double ZOOM_BASE = 1.6;

    private static final double MOUSE_WHEEL_SENSITIVITY = 120;

    private static BooleanSetting transparentSelection = new BooleanSetting("transparentSelection");

    private static ColorArraySetting customUIBaseColors = new ColorArraySetting("customUIColors");

    private ImageFileManager imageFileManager;

    // private Image image;
    // private ImageFile imageFile;

    private SelectionManager selectionManager;

    /**
     * used for restoring the tool that was selected before the color picker
     */
    private ImageTool prevTool;
    private ImageTool activeTool;

    private int primaryColor;
    private int secondaryColor;
    /**
     * ColorPicker for the currently selected color
     */
    private ColorPicker selectedColorPicker;
    /**
     * 0 = primary color is selected, 1 = secondary color is selcted
     */
    private int colorSelection;
    private ColorArray customColorButtonArray;

    private SVector imageTranslation;
    private int imageZoomLevel;
    private boolean draggingImage;

    private ImageCanvas canvas;

    // private ColorEditorApp colorEditorApp;
    // private SettingsApp settingsApp;

    public MainApp() {
        super((int) Sizes.MAIN_APP.width, (int) Sizes.MAIN_APP.height, Window.MAXIMIZED, "SLPaint");

        window.setCloseOnEscape(false);

        Settings.loadSettings();

        // customColors = new ArrayList<>();
        customColorButtonArray = new ColorArray(MainUI.NUM_COLOR_BUTTONS_PER_ROW);
        selectionManager = new SelectionManager(this);

        // imageFileManager = new ImageFileManager(this);
        imageFileManager = new ImageFileManager(this, "test.png");

        primaryColor = SUtil.toARGB(0);
        secondaryColor = SUtil.toARGB(255);
        colorSelection = PRIMARY_COLOR;
        selectedColorPicker = new ColorPicker(this, getSelectedColor(), color -> addCustomColor(color));

        createUI();

        renderer = new MainAppRenderer(this);

        resetImageTransform();

        activeTool = ImageTool.PENCIL;
        prevTool = ImageTool.PENCIL;
    }

    @Override
    public void update(double deltaT) {
        super.update(deltaT);

        boolean[] keys = window.getKeys();
        boolean[] prevKeys = window.getPrevKeys();
        SVector mousePos = window.getMousePosition();
        SVector prevMousePos = window.getPrevMousePosition();
        boolean[] mouseButtons = window.getMouseButtons();
        boolean[] prevMouseButtons = window.getPrevMouseButtons();
        SVector mouseScroll = window.getMouseScroll();
        SVector prevMouseScroll = window.getPrevMouseScroll();

        int[] mouseImagePos = getMouseImagePosition();
        int mouseX = mouseImagePos[0];
        int mouseY = mouseImagePos[1];

        Image image = imageFileManager.getImage();
        // canvas actions
        if (canvas.mouseTrulyAbove()) {
            SVector scrollAmount = new SVector(mouseScroll).sub(prevMouseScroll).scale(MOUSE_WHEEL_SENSITIVITY);
            boolean shiftPressed = keys[GLFW.GLFW_KEY_LEFT_CONTROL];
            if (shiftPressed) {
                // zoom
                double prevZoom = getImageZoom();
                imageZoomLevel += (int) Math.signum(scrollAmount.y);
                imageZoomLevel = Math.min(Math.max(MIN_ZOOM_LEVEL, imageZoomLevel), MAX_ZOOM_LEVEL);
                double zoom = getImageZoom();
                imageTranslation.set(imageTranslation.sub(mousePos).scale(zoom / prevZoom).add(mousePos));

                window.setHandCursor();
            } else {
                // scroll
                imageTranslation.add(scrollAmount);
            }

            // selection - left click
            if (mouseButtons[0] && !prevMouseButtons[0]) {
                if (selectionManager.getPhase() == SelectionManager.IDLE) {
                    if (selectionManager.mouseAboveSelection(mouseX, mouseY)) {
                        // start dragging selection
                        selectionManager.startDragging();
                    } else {
                        // finish selection
                        selectionManager.finish();
                    }
                } else if (activeTool == ImageTool.SELECTION) {
                    // start creating selection
                    selectionManager.startCreating();
                }
            }

            // right click
            if (mouseButtons[1] && !prevMouseButtons[1]) {
                if (keys[GLFW.GLFW_KEY_LEFT_CONTROL]) {
                    // start dragging image
                    draggingImage = true;
                }
            }

            // tool click
            if (!keys[GLFW.GLFW_KEY_LEFT_CONTROL]) {
                if (image.isInside(mouseX, mouseY)) {
                    boolean anyClickHappened = false;
                    for (int i = 0; i < mouseButtons.length; i++) {
                        if (mouseButtons[i] && !prevMouseButtons[i]) {
                            activeTool.click(this, mouseX, mouseY, i);
                            anyClickHappened = true;
                        }
                    }
                    if (activeTool == ImageTool.COLOR_PICKER && anyClickHappened) {
                        // queue the resetting to the previous tool to avoid using the previous tool for
                        // one frame
                        queueEvent(() -> setActiveTool(prevTool));
                    }
                }
            }
        }

        // tool update / release
        for (int i = 0; i < mouseButtons.length; i++) {
            if (mouseButtons[i]) {
                if (!keys[GLFW.GLFW_KEY_LEFT_CONTROL]) {
                    // tool update
                    if (canvas.mouseTrulyAbove()) {
                        activeTool.update(this, mouseX, mouseY, i);
                    }
                }
            } else if (prevMouseButtons[i]) {
                // tool release
                activeTool.release(this, mouseX, mouseY, i);
            }
        }

        // stop dragging image
        if (!mouseButtons[1] || !keys[GLFW.GLFW_KEY_LEFT_CONTROL]) {
            draggingImage = false;
        }
        // dragging image
        if (draggingImage) {
            SVector mouseMovement = new SVector(mousePos).sub(prevMousePos);
            imageTranslation.add(mouseMovement);
        }

        // selection actions
        switch (selectionManager.getPhase()) {
            case SelectionManager.CREATING -> {
                // finish creating selection
                if (!mouseButtons[0]) {
                    selectionManager.finishCreating();
                }
            }
            case SelectionManager.IDLE -> {
                // esc -> finish selection
                if (keyPressed(keys, prevKeys, GLFW.GLFW_KEY_CAPS_LOCK)) {
                    selectionManager.finish();
                }
                // del -> cancel selection
                if (keyPressed(keys, prevKeys, GLFW.GLFW_KEY_DELETE)) {
                    selectionManager.cancel();
                }
            }
            case SelectionManager.DRAGGING -> {
                // finish dragging image
                if (!mouseButtons[0]) {
                    selectionManager.finishDragging();
                }
            }
        }

        // keyboard shortcuts
        if (keyPressed(keys, prevKeys, GLFW.GLFW_KEY_R) && !keys[GLFW.GLFW_KEY_LEFT_SHIFT]) {
            // R -> reset viewport transform
            resetImageTransform();
        }

        if (keys[GLFW.GLFW_KEY_LEFT_CONTROL]) {
            // Ctrl + A -> select everything
            if (keyPressed(keys, prevKeys, GLFW.GLFW_KEY_A)) {
                setActiveTool(ImageTool.SELECTION);
                selectionManager.selectEverything();
            }

            // Ctrl + V -> paste clipboard
            if (keyPressed(keys, prevKeys, GLFW.GLFW_KEY_V)) {
                BufferedImage paste = ClipboardManager.getImage();
                if (paste != null) {
                    setActiveTool(ImageTool.SELECTION);
                    selectionManager.selectClipboard(paste);
                }
            }

            // Ctrl + C -> copy to clipboard
            if (keyPressed(keys, prevKeys, GLFW.GLFW_KEY_C)) {
                if (selectionManager.getPhase() == SelectionManager.IDLE) {
                    ClipboardManager.setImage(selectionManager.getSelection().getBufferedImage());
                }
            }

            // Ctrl + X -> cut to clipboard
            if (keyPressed(keys, prevKeys, GLFW.GLFW_KEY_X)) {
                if (selectionManager.getPhase() == SelectionManager.IDLE) {
                    ClipboardManager.setImage(selectionManager.getSelection().getBufferedImage());
                    selectionManager.cancel();
                }
            }

            // Ctr (+ Shift) + S -> save (as)
            if (keyPressed(keys, prevKeys, GLFW.GLFW_KEY_S)) {
                if (keys[GLFW.GLFW_KEY_LEFT_SHIFT]) {
                    saveImageAs();
                } else {
                    saveImage();
                }
            }
        }

        selectedColorPicker.update();

        if (colorSelection == PRIMARY_COLOR) {
            primaryColor = selectedColorPicker.getRGB();
        } else {
            secondaryColor = selectedColorPicker.getRGB();
        }

        selectionManager.update();

        // update image texture
        image.updateOpenGLTexture();
    }

    @Override
    public void finish() {
        super.finish();

        Settings.finish();
    }

    public void selectColor(int color) {
        if (colorSelection == PRIMARY_COLOR) {
            setPrimaryColor(color);
            // primaryColor = color;
        } else {
            setSecondaryColor(color);
            // secondaryColor = color;
        }
    }

    public void addCustomColor(int color) {
        selectColor(color);
        // if (colorSelection == 0) {
        // primaryColor = color;
        // } else {
        // secondaryColor = color;
        // }

        customColorButtonArray.addColor(color);
    }

    public void drawLine(int x, int y, int color) {
        Image image = imageFileManager.getImage();

        int[] prevMousePos = getMouseImagePosition(window.getPrevMousePosition());
        int x0 = prevMousePos[0];
        int y0 = prevMousePos[1];

        int dx = Math.abs(x - x0);
        int dy = Math.abs(y - y0);
        if (dx > dy) {
            if (x0 > x) {
                int temp = x0;
                x0 = x;
                x = temp;

                temp = y0;
                y0 = y;
                y = temp;
            }
            for (int t = x0; t <= x; t++) {
                int py = x0 == x ? y0 : (int) Math.round(SUtil.map(t, x0, x, y0, y));
                image.setPixel(t, py, color);
            }
        } else {
            if (y0 > y) {
                int temp = y0;
                y0 = y;
                y = temp;

                temp = x0;
                x0 = x;
                x = temp;
            }
            for (int t = y0; t <= y; t++) {
                int px = y0 == y ? x0 : (int) Math.round(SUtil.map(t, y0, y, x0, x));
                image.setPixel(px, t, color);
            }
        }
    }

    public SVector[] getImageViewport() {
        SVector pos = canvas.getAbsolutePosition();
        SVector size = canvas.getSize();
        return new SVector[] { new SVector(pos.x + 1, pos.y + 1), new SVector(pos.x + size.x - 1, pos.y + size.y - 1) };
    }

    public void showDialog(int type) {
        super.showDialog(type);
        switch (type) {
            case SAVE_DIALOG -> (new SaveDialog(this)).start();
            case UNABLE_TO_SAVE_IMAGE_DIALOG -> (new UnableToSaveImageDialog(this)).start();
            case NEW_COLOR_DIALOG, SETTINGS_DIALOG -> {
            }
            default -> (new UnimplementedDialog(this, type)).start();
        }
    }

    @Override
    protected App createChildApp(int dialogType) {
        return switch (dialogType) {
            case NEW_COLOR_DIALOG -> new ColorEditorApp(
                    this,
                    getSelectedColor());
            // colorSelection == 0 ? primaryColor : secondaryColor);
            case SETTINGS_DIALOG ->
                new SettingsApp(this);
            default -> null;
        };
    }

    public void openImage() {
        imageFileManager.open();
    }

    public void newImage() {
        Image image = imageFileManager.getImage();
        imageFileManager.newImage(image.getWidth(), image.getHeight());
    }

    /**
     * Gets called when Ctrl+S is pressed
     */
    public void saveImage() {
        imageFileManager.save();
    }

    /**
     * Gets called when Ctrl+Shift+S is pressed
     * 
     */
    public void saveImageAs() {
        imageFileManager.saveAs();
    }

    public static ColorArray getCustomUIBaseColors() {
        return customUIBaseColors.get();
    }

    public static void addCustomUIBaseColor(int color) {
        customUIBaseColors.get().addColor(color);
    }

    public void resetImageTransform() {
        imageZoomLevel = 0;
        imageTranslation = canvas.getAbsolutePosition().add(new SVector(1, 1).scale(canvas.getMargin()));
    }

    public SelectionManager getSelectionManager() {
        return selectionManager;
    }

    public Image getImage() {
        return imageFileManager.getImage();
    }

    public SVector getImageTranslation() {
        return imageTranslation;
    }

    public double getImageZoom() {
        return Math.pow(ZOOM_BASE, imageZoomLevel) * Sizes.getUIScale();
    }

    public void setCanvas(ImageCanvas element) {
        canvas = element;
    }

    public ColorPicker getSelectedColorPicker() {
        return selectedColorPicker;
    }

    public void setPrimaryColor(int primaryColor) {
        this.primaryColor = primaryColor;
        selectedColorPicker.setRGB(primaryColor);
    }

    public int getPrimaryColor() {
        return primaryColor;
    }

    public void setSecondaryColor(int secondaryColor) {
        this.secondaryColor = secondaryColor;
        selectedColorPicker.setRGB(secondaryColor);
    }

    public int getSecondaryColor() {
        return secondaryColor;
    }

    public int getSelectedColor() {
        return colorSelection == PRIMARY_COLOR ? primaryColor : secondaryColor;
    }

    public int getColorSelection() {
        return colorSelection;
    }

    public void setColorSelection(int colorSelection) {
        this.colorSelection = colorSelection;

        selectedColorPicker.setRGB(getSelectedColor());
    }

    public ImageTool getActiveTool() {
        return this.activeTool;
    }

    public void setActiveTool(ImageTool tool) {
        if (activeTool == tool) {
            return;
        }
        if (activeTool == ImageTool.SELECTION) {
            if (selectionManager.getPhase() == SelectionManager.IDLE) {
                selectionManager.finish();
            }
        }
        if (tool == ImageTool.COLOR_PICKER) {
            prevTool = activeTool;
        }
        activeTool = tool;
    }

    public static boolean isTransparentSelection() {
        return transparentSelection.get();
    }

    public static void toggleTransparentSelection() {
        transparentSelection.set(!transparentSelection.get());
    }

    public int[] getMouseImagePosition() {
        return getMouseImagePosition(window.getMousePosition());
    }

    private int[] getMouseImagePosition(SVector mouse) {
        SVector mouseImagePos = mouse.copy().sub(imageTranslation).div(getImageZoom());
        return new int[] { (int) Math.floor(mouseImagePos.x), (int) Math.floor(mouseImagePos.y) };
    }

    public SVector getMouseImagePosVec() {
        return window.getMousePosition().copy().sub(imageTranslation).div(getImageZoom());
    }

    public int getMouseImageX() {
        return getMouseImagePosition()[0];
    }

    public int getMouseImageY() {
        return getMouseImagePosition()[1];
    }

    public int[] getDisplaySize() {
        return window.getDisplaySize();
    }

    public ColorArray getCustomColorButtonArray() {
        return customColorButtonArray;
    }

    public long getFilesize() {
        ImageFile imageFile = imageFileManager.getImageFile();
        return imageFile == null ? -1 : imageFile.getSize();
    }

    public ImageFormat getImageFormat() {
        ImageFile imageFile = imageFileManager.getImageFile();
        return imageFile == null ? null : imageFile.getFormat();
    }

    public String getFilename() {
        ImageFile imageFile = imageFileManager.getImageFile();
        return imageFile == null ? "" : imageFile.getFile().getName();
    }

    public ImageFile getImageFile() {
        return imageFileManager.getImageFile();
    }

    public static String formatFilesize(long filesize) {
        final String[] prefixes = { "k", "M", "G", "T" };
        long remainder = 0;
        for (int i = 0; i < prefixes.length; i++) {
            if (filesize < 1024) {
                if (i == 0) {
                    return filesize + "B";
                } else {
                    String unit = prefixes[i - 1] + "B";
                    return "%d.%02d%s".formatted(filesize, (remainder * 100) / 1024, unit);
                }
            }
            remainder = (filesize & 0x3FF);
            filesize = filesize >> 10;
        }
        return "[Filesize too large!]";
    }

    public static int toInt(SVector color) {
        return SUtil.toARGB(color.x * 255, color.y * 255, color.z * 255);
    }

    public static SVector toSVector(Integer color) {
        if (color == null) {
            return null;
        }
        int red = SUtil.red(color);
        int green = SUtil.green(color);
        int blue = SUtil.blue(color);
        return new SVector(red, green, blue).div(255);
    }

    public static int runCommand(String directory, ArrayList<String> commands) {
        int exitVal = 1;
        try {
            // ProcessBuilder pb = new ProcessBuilder("sh", "-c", "ls");
            ProcessBuilder pb = new ProcessBuilder(commands);
            // pb.directory(new File(System.getProperty("user.home")));
            pb.directory(new File(directory));
            Process process = pb.start();

            StringBuilder output = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line + "\n");
            }

            exitVal = process.waitFor();
            // if (exitVal == 0) {
            // System.out.println(output);
            // }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return exitVal;
    }
}