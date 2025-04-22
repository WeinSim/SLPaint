package main.apps;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.lwjgl.glfw.GLFW;

import main.ColorArray;
import main.ColorPicker;
import main.Image;
import main.ImageFile;
import main.ImageFileManager;
import main.ImageFormat;
import main.dialogs.SaveDialog;
import main.dialogs.UnableToSaveImageDialog;
import main.dialogs.UnimplementedDialog;
import main.settings.BooleanSetting;
import main.settings.ColorArraySetting;
import main.settings.Settings;
import main.tools.ImageTool;
import renderEngine.MainAppRenderer;
import renderEngine.Window;
import renderEngine.fonts.TextFont;
import sutil.SUtil;
import sutil.math.SVector;
import sutil.ui.UITextInput;
import ui.MainUI;
import ui.Sizes;
import ui.components.ImageCanvas;

/**
 * <pre>
 * TODO continue:
 *   Try to get this mess of a new rendering system to work
 *   Proper layer management in MainAppRenderer for non-ui stuff (image, tools,
 *     selection, etc.)
 *   Turn all remaining draw calls (hsl, image) into a single VAO each
 * Text tool:
 *   Text rendering:
 *     Text wrapping / new lines with ENTER (in combination with proper UI text
 *       input?)
 *     TextTool.MIN_TEXT_SIZE should be set to 1, not 0. However, currently the
 *       UI doesn't allow to input single-digit values if the minimum is not 0.
 *       Create an interface or similar for number UIInputs?
 * 
 * App:
 *   Small problems:
 *     Pressing 'r' should not reset the view if a text input is currently
 *       active
 *     ImageCanvas.mouseTrulyAbove() should return true even if the mouse is
 *       above the text tool input
 *   Pencil
 *     Add different sizes
 *   Line tool
 *   Text tool
 *     When the size of the image changes, the FBO's texture should also
 *       update its size.
 *   Seelction tool
 *     Add ability to move selection with arrow keys (keyboard shortcuts)
 *     Selection resizing
 *     Selection Ctrl+Shift+X
 *   Image resizing
 *   Undo / redo
 *   Transparency
 *     Grey-white squares should always appear the same size
 *     Simply selecting a transparent area and unselecting it causes the
 *       transparency to go away. Reason: selecting the area replaces that part
 *       of the image with the secondary color. Placing the transparent
 *       selection back onto the opaque background leaves the background
 *       unaffected. What is the expected behavior here?
 *   Dialogs
 *     Save dialog
 *       Keep track of unsaved changes, ask user to save before quitting if
 *       there are unsaved changes
 *     Only one at a time
 *     Keep track of all file locks in one centralized place to avoid leaking
 *   Recognize remapping from CAPS_LOCK to ESCAPE
 *   (When parent app closes, shouldren should also close)
 *   ColorPicker: hue values still sometimes show up negative
 *   Pixels with an alpha value of 0 lose color information when saving and
 *     reopening
 * UI:
 *   Shift + mouse wheel should scroll horizontally
 *   Fix bug in UILabel: when the textUpdater returns text containig newline
 *     characters, the text is not properly split across multiple lines
 *   Tool icons & cursors
 *   Make side panel collapsable
 * Rendering:
 *   The outline of the text size input is sometimes cut off by the plus button
 *     next to it (depending on the horizontal scroll)
 *   Text rendering
 *     How to handle fonts?
 *       How to handle big font sizes?
 *         Generate texture atlas using fontbm on demand?
 *         Use SDFs (either in addition to or instead of regular bitmap fonts)?
 *     Text renders inconsistently: some letters are blurry and other are not.
 *       For example, using Courier New Bold with a rasterized text size of 32,
 *       the letters 'e', 'r', 'i' and 'd' are blurry, whereas 'p', 'u', 'm'
 *       and 'b' are sharp. (it seems like most blurry letters are on page 2.)
 *     Potential speedups for text rendering:
 *       Offload matrix transforms of vertex data in text VAO to shader (see
 *         comment in TextFont.createGiantVAO())
 *       Cache conversion from String to FontChar[] in TextFont
 *       Only override the parts of the text VAOs that actually change from one
 *         frame to the next
 *   Anti aliasing doesn't work despite being enabled
 *     (glfwWindowHint(GLFW_SAMPLES, 4) and glEnable(GL_MULTISAMPLE))
 *   Selection border sometimes has artifacts on bottom and right inner edges
 *   AlphaScale has artifacts on bottom edge (whose size depends on wether a
 *     text cursor is currently visible?!?)
 *   Fix stuttering artifact when resizing windows on Linux
 *     (see https://www.glfw.org/docs/latest/window.html#window_refresh)
 *   Clean up UIRenderMaster API and UI shaders (especially with regards to
 *     transparency and text rendering)
 *     Premultiply view matrix and transformation matrix (for rect shader)
 *     Rename transformationMatrix to uiMatrix
 *   Merge rect shaders? (95% of their code is identical)
 *   Remove magic numbers in {@link renderEngine.MainAppRenderer#render()}
 *   "Activate alpha blending" in
 *     {@link renderEngine.UIRenderMaster#image(int, SVector, SVector)}
 *     (whatever that means??)
 * Maximized windows don't show up correctly on Windows 11
 * Error handling
 * UI extras: (optional)
 *   3D view
 *   Debug view
 * </pre>
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

    private static final double MOUSE_WHEEL_SENSITIVITY = 100;

    private static BooleanSetting transparentSelection = new BooleanSetting("transparentSelection");

    private static ColorArraySetting customUIBaseColors = new ColorArraySetting("customUIColors");

    private ImageFileManager imageFileManager;

    /**
     * used for restoring the tool that was selected before the color picker
     */
    private ImageTool prevTool;
    private ImageTool activeTool;

    private UITextInput textToolInput;

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

    public MainApp() {
        super((int) Sizes.MAIN_APP.width, (int) Sizes.MAIN_APP.height, Window.MAXIMIZED, "SLPaint");

        closeOnEscape = false;

        customColorButtonArray = new ColorArray(MainUI.NUM_COLOR_BUTTONS_PER_ROW);

        imageFileManager = new ImageFileManager(this, "test.png");

        primaryColor = SUtil.toARGB(0);
        secondaryColor = SUtil.toARGB(255);
        colorSelection = PRIMARY_COLOR;
        selectedColorPicker = new ColorPicker(getSelectedColor());

        createUI();

        renderer = new MainAppRenderer(this);

        imageTranslation = new SVector();

        ImageTool.init(this);

        setActiveTool(ImageTool.SELECTION);
        prevTool = ImageTool.SELECTION;
    }

    @Override
    public void update(double deltaT) {
        super.update(deltaT);

        if (frameCount == 1) {
            // Calling this in setup() does not work because the UI has not been updated
            // yet, so the absolute position of the image canvas it still (0, 0).
            resetImageTransform();
        }

        int[] mouseImagePos = getMouseImagePosition();
        int mouseX = mouseImagePos[0];
        int mouseY = mouseImagePos[1];

        // tool update / release
        for (int i = 0; i < mouseButtons.length; i++) {
            if (mouseButtons[i]) {
                if (!keys[GLFW.GLFW_KEY_LEFT_CONTROL]) {
                    // tool update
                    int[] pMouse = getMouseImagePosition(prevMousePos);
                    activeTool.mouseDragged(mouseX, mouseY, pMouse[0], pMouse[1], i);
                }
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

        if (colorSelection == PRIMARY_COLOR) {
            primaryColor = selectedColorPicker.getRGB();
        } else {
            secondaryColor = selectedColorPicker.getRGB();
        }

        // update image texture
        getImage().updateOpenGLTexture();
    }

    @Override
    protected void keyPressed(int key, int mods) {
        super.keyPressed(key, mods);

        // tool keyboard shortcuts
        for (ImageTool tool : ImageTool.INSTANCES) {
            tool.keyPressed(key, mods);
        }

        switch (key) {
            // R -> reset image transform
            case GLFW.GLFW_KEY_R -> {
                if ((mods & GLFW.GLFW_MOD_SHIFT) == 0) {
                    resetImageTransform();
                }
            }

            // Ctrl + (Shift +) S -> save (as)
            case GLFW.GLFW_KEY_S -> {
                if ((mods & GLFW.GLFW_MOD_CONTROL) != 0) {
                    if ((mods & GLFW.GLFW_MOD_SHIFT) == 0) {
                        saveImage();
                    } else {
                        saveImageAs();
                    }
                }
            }
        }
    }

    @Override
    protected void mousePressed(int button, int mods) {
        super.mousePressed(button, mods);

        if (canvas.mouseTrulyAbove()) {
            int[] mousePos = getMouseImagePosition();
            int mouseX = mousePos[0],
                    mouseY = mousePos[1];

            // tool click
            if ((mods & GLFW.GLFW_MOD_CONTROL) == 0) {
                activeTool.mousePressed(mouseX, mouseY, button);
            }

            switch (button) {
                // start dragging image
                case GLFW.GLFW_MOUSE_BUTTON_RIGHT -> {
                    if ((mods & GLFW.GLFW_MOD_CONTROL) != 0) {
                        draggingImage = true;
                    }
                }
            }
        }
    }

    @Override
    protected void mouseReleased(int button, int mods) {
        super.mouseReleased(button, mods);

        activeTool.mouseReleased(button);
    }

    @Override
    protected void mouseScroll(double xoff, double yoff) {
        super.mouseScroll(xoff, yoff);

        SVector mousePos = window.getMousePosition();

        // canvas actions
        if (canvas.mouseTrulyAbove()) {
            SVector scrollAmount = new SVector(xoff, yoff).scale(MOUSE_WHEEL_SENSITIVITY);
            boolean ctrlPressed = keys[GLFW.GLFW_KEY_LEFT_CONTROL];
            if (ctrlPressed) {
                // zoom
                double prevZoom = getImageZoom();
                imageZoomLevel += (int) Math.signum(scrollAmount.y);
                imageZoomLevel = Math.min(Math.max(MIN_ZOOM_LEVEL, imageZoomLevel), MAX_ZOOM_LEVEL);
                double zoom = getImageZoom();
                imageTranslation.set(imageTranslation.sub(mousePos).scale(zoom / prevZoom).add(mousePos));
                window.setHandCursor();
            } else {
                // scroll
                boolean shiftPressed = keys[GLFW.GLFW_KEY_LEFT_SHIFT] || keys[GLFW.GLFW_KEY_RIGHT_SHIFT];
                if (shiftPressed) {
                    double temp = scrollAmount.x;
                    scrollAmount.x = scrollAmount.y;
                    scrollAmount.y = temp;
                }
                imageTranslation.add(scrollAmount);
            }
        }
    }

    @Override
    public void finish() {
        super.finish();

        Settings.finish();
    }

    public void renderTextToImage(String text, int x, int y, int size, TextFont font) {
        renderer.renderTextToImage(text, x, y, size, toSVector(primaryColor), font, getImage());
    }

    public void selectColor(int color) {
        if (colorSelection == PRIMARY_COLOR) {
            setPrimaryColor(color);
        } else {
            setSecondaryColor(color);
        }
    }

    public void addCustomColor(int color) {
        selectColor(color);
        customColorButtonArray.addColor(color);
    }

    public void drawLine(int x, int y, int x0, int y0, int color) {
        Image image = imageFileManager.getImage();

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
        imageTranslation = canvas.getAbsolutePosition().add(new SVector(canvas.getHMargin(), canvas.getVMargin()));
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

    public ImageCanvas getCanvas() {
        return canvas;
    }

    public UITextInput getTextToolInput() {
        return textToolInput;
    }

    public void setTextToolInput(UITextInput textToolInput) {
        this.textToolInput = textToolInput;
    }

    public ColorPicker getSelectedColorPicker() {
        return selectedColorPicker;
    }

    public void setPrimaryColor(int primaryColor) {
        if (colorSelection == 0) {
            selectedColorPicker.setRGB(primaryColor);
        } else {
            this.primaryColor = primaryColor;
        }
    }

    public int getPrimaryColor() {
        return primaryColor;
    }

    public void setSecondaryColor(int secondaryColor) {
        if (colorSelection == 1) {
            selectedColorPicker.setRGB(secondaryColor);
        } else {
            this.secondaryColor = secondaryColor;
        }
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

        // quit old tool
        if (activeTool != null) {
            activeTool.forceQuit();
            if (tool == ImageTool.PIPETTE) {
                prevTool = activeTool;
            }
        }

        // start new tool
        activeTool = tool;
        // activeTool.start();
    }

    public void switchBackToPreviousTool() {
        queueEvent(() -> setActiveTool(prevTool));
    }

    public static boolean isTransparentSelection() {
        return transparentSelection.get();
    }

    public static void setTransparentSelection(boolean transparentSelection) {
        MainApp.transparentSelection.set(transparentSelection);
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