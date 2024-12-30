package main.apps;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.lwjgl.glfw.GLFW;

import main.ColorButtonArray;
import main.Image;
import main.ImageFile;
import main.ImageFileManager;
import main.ImageFormat;
import main.ImageTool;
import main.SelectionManager;
import main.dialogs.SaveDialog;
import main.dialogs.UnableToSaveImageDialog;
import main.dialogs.UnimplementedDialog;
import renderEngine.MainAppRenderer;
import sutil.SUtil;
import sutil.math.SVector;
import sutil.ui.UIContainer;
import ui.MainUI;

/**
 * TODO: continue: light mode / dark mode
 * App:
 * * When parent app closes, shouldren should also close
 * * Dialogs
 * * * Save dialog
 * * * * Keep track of unsaved changes, ask user to save before quitting if
 * * * * there are unsaved changes
 * * * Only one at a time
 * * Keep track of all file locks in one centralized place to avoid leaking
 * * Selection
 * * * Clipboard (Copy / Paste / Cut)
 * * * Transparent selection (checkbox)
 * * Resizing
 * * * Resize button
 * * * Resize handles
 * * * Selection resizing
 * * * Selection Ctrl+Shift+X
 * * Undo / redo
 * * Pencil: different sizes
 * * Text tool
 * * Transparency?
 * * Settings > UI Base Color
 * * * When clicked: color picker, default colors, custom colors
 * * Recognize remapping from CAPS_LOCK to ESCAPE
 * * Maybe turn the ColorPickerApp into a togglable side panel?
 * UI:
 * * Light theme / dark theme
 * * General design: containers with separators should double their margin!
 * * Scrolling
 * * Tool icons & cursors
 * * (Add hMargin and vMargin in UIContainer)
 * * * Not neccessary for now. UISeparators now extend fully even without
 * * * hMargin and vMargin
 * Rendering:
 * * Ellipse rendering (for color buttons maybe?)
 * * Premultiply view matrix and transformation matrix (for rect and text
 * * shader)
 * * Rename transformationMatrix to uiMatrix
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

    private static final int MIN_ZOOM_LEVEL = -4;
    private static final int MAX_ZOOM_LEVEL = 8;
    private static final double ZOOM_BASE = 1.6;

    private static final double MOUSE_WHEEL_SENSITIVITY = 120;

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
     * 0 = primary color is selected, 1 = secondary color is selcted
     */
    private int colorSelection;
    // private ArrayList<Integer> customColors;
    private ColorButtonArray customColorButtonArray;
    // private CustomColorContainer customColorContainer;

    private SVector imageTranslation;
    private int imageZoomLevel;
    private boolean draggingImage;

    private UIContainer canvas;

    // private ColorEditorApp colorEditorApp;
    // private SettingsApp settingsApp;
    private ArrayList<Integer> customUIBaseColors;

    public MainApp() {
        super(1280, 720, 1, "SLPaint");

        window.setCloseOnEscape(false);

        // customColors = new ArrayList<>();
        customColorButtonArray = new ColorButtonArray(MainUI.NUM_COLOR_BUTTONS_PER_ROW);
        customUIBaseColors = new ArrayList<>();
        selectionManager = new SelectionManager(this);

        // imageFileManager = new ImageFileManager(this);
        imageFileManager = new ImageFileManager(this, "dialogFlowchart.png");

        createUI();

        renderer = new MainAppRenderer(this);

        resetImageTransform();

        activeTool = ImageTool.PENCIL;
        prevTool = ImageTool.PENCIL;

        primaryColor = 0;
        secondaryColor = -1;
    }

    @Override
    public void update(double deltaT) {
        super.update(deltaT);

        // App.setBaseColor(toSVector(primaryColor));

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
        if (canvas.mouseAbove()) {
            // scroll actions
            double scrollAmount = (prevMouseScroll.y - mouseScroll.y) * MOUSE_WHEEL_SENSITIVITY;
            if (keys[GLFW.GLFW_KEY_LEFT_CONTROL]) {
                // zoom
                double prevZoom = getImageZoom();
                imageZoomLevel -= (int) Math.signum(scrollAmount);
                imageZoomLevel = Math.min(Math.max(MIN_ZOOM_LEVEL, imageZoomLevel), MAX_ZOOM_LEVEL);
                double zoom = getImageZoom();
                imageTranslation.set(imageTranslation.sub(mousePos).scale(zoom / prevZoom).add(mousePos));

                window.setHandCursor();
            } else {
                // scroll
                if (keys[GLFW.GLFW_KEY_LEFT_SHIFT]) {
                    imageTranslation.x -= scrollAmount;
                } else {
                    imageTranslation.y -= scrollAmount;
                }
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
                    if (canvas.mouseAbove()) {
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
            // Ctr (+ Shift) + S -> save (as)
            if (keyPressed(keys, prevKeys, GLFW.GLFW_KEY_S)) {
                if (keys[GLFW.GLFW_KEY_LEFT_SHIFT]) {
                    saveImageAs();
                } else {
                    saveImage();
                }
            }
        }

        selectionManager.update();

        // update image texture
        image.updateOpenGLTexture();
    }

    @Override
    public void finish() {
    }

    public void selectColor(int color) {
        if (colorSelection == 0) {
            primaryColor = color;
        } else {
            secondaryColor = color;
        }
    }

    public void addCustomColor(int color) {
        if (colorSelection == 0) {
            primaryColor = color;
        } else {
            secondaryColor = color;
        }

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
                    colorSelection == 0 ? primaryColor : secondaryColor);
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

    public ArrayList<Integer> getCustomUIBaseColors() {
        return customUIBaseColors;
    }

    public void resetImageTransform() {
        imageZoomLevel = 0;
        imageTranslation = getCanvasPosition().add(new SVector(1, 1).scale(getCanvasMargin()));
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
        return Math.pow(ZOOM_BASE, imageZoomLevel);
    }

    public void setCanvas(UIContainer element) {
        canvas = element;
    }

    public SVector getCanvasPosition() {
        return canvas.getAbsolutePosition();
    }

    public double getCanvasMargin() {
        return canvas.getMargin();
    }

    public void setPrimaryColor(int primaryColor) {
        this.primaryColor = primaryColor;
    }

    public int getPrimaryColor() {
        return primaryColor;
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

    public void setSecondaryColor(int secondaryColor) {
        this.secondaryColor = secondaryColor;
    }

    public int getSecondaryColor() {
        return secondaryColor;
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

    public int getColorSelection() {
        return colorSelection;
    }

    public void setColorSelection(int colorSelection) {
        this.colorSelection = colorSelection;
    }

    public int[] getDisplaySize() {
        return window.getDisplaySize();
    }

    public ColorButtonArray getCustomColorButtonArray() {
        return customColorButtonArray;
    }

    // public void setCustomColorContainer(CustomColorContainer
    // customColorContainer) {
    // this.customColorContainer = customColorContainer;
    // }

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