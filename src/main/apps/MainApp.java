package main.apps;

import static org.lwjgl.glfw.GLFW.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.lwjglx.util.vector.Vector4f;

import main.ColorArray;
import main.ColorPicker;
import main.image.Image;
import main.image.ImageFormat;
import main.image.ImageManager;
import main.settings.BooleanSetting;
import main.settings.ColorArraySetting;
import main.settings.Settings;
import main.tools.ImageTool;
import renderengine.Window;
import renderengine.fonts.TextFont;
import sutil.SUtil;
import sutil.math.SVector;
import sutil.ui.elements.UITextInput;
import ui.AppUI;
import ui.MainUI;
import ui.components.ImageCanvas;

/**
 * <pre>
 * TODO continue:
 *   File management
 *     Pop-ups with UI ("Modal dialogs")
 *       Add semi-transparent background over entire window as float
 *         container on high layer (with message box above it)
 *       Add UI.showPopUp or similar to replace JOptionPane.showOptionMessage
 *     Only move things to another thread when really neccessary
 *     Ask user to save changes when window is closed
 *     Testing
 *     Does it make sense to have file filters for different types of image
 *       files when the format of a saved file only depends on the given
 *       filename?
 * 
 * App:
 *   Line tool
 *   Pencil tool
 *     Sizes 1 & 2 and 3 & 4 look the same
 *     Make sizes UI prettier
 *     Drawing with a semi-transparent color has weird artifacts because some
 *       pixels are drawn multiple times on consecutive frames, resulting in
 *       the wrong opacity
 *       => Make the pencil tool also use the temp framebuffer?
 *   Selection tool
 *     Add flip and rotate options for selection
 *   Keyboard shortcuts
 *     Selecting one of the radio buttons in the resize ui and pressing enter
 *       closes the resize window. => add option for keyboard shortcut to not
 *       run if something is currently selected (similar to text input).
 *   No child apps should be open when the image changes?
 *   Transparency
 *     Selecting a semi-transparent area and pasting it over a completely
 *       transparent area messes up the pixel colors: the semi-transparent area
 *       picks up the "color" (RGB values) of the transparent background.
 *     Simply selecting a transparent area and unselecting it causes the
 *       transparency to go away. Reason: selecting the area replaces that part
 *       of the image with the secondary color. Placing the transparent
 *       selection back onto the opaque background leaves the background
 *       unaffected. What is the expected behavior here?
 *     Pixels with an alpha value of 0 are considered different if they differ
 *       in their color information. What is the expected behavior here?
 *     Pixels with an alpha value of 0 lose color information when saving and
 *       reopening. (This is a property of the .png file format that can be
 *       changed I think (?). Also, what is the expected behavior?)
 *     Pasting / drawing a half-transparent red pixel over a fully opaque green
 *       one results in brown overlap instead of yellow (see MinutePhysics
 *         video)
 *       => Add correct gamma blending? (as a setting?)
 *         Have OpenGL also do correct gamma blending?
 *   Text input
 *     Proper number input
 *     Selection (with mouse / arrow keys / Ctrl+A)
 *     Copy / cut / paste
 *     Shift + cursor movement
 *     Multi-line text input
 *       Would require a variable number of UITexts as children (which is
 *         currently not possible. Why?)
 *   (When parent app closes, children should also close)
 *   Implement own version of JOptionPane and JFileChooser using UI classes
 * 
 * UI:
 *   Text wrapping (see "Text input")
 *   Fix bug in UILabel: when the textUpdater returns text containig newline
 *     characters, the text is not properly split across multiple lines
 *   Tool icons & cursors
 *   Make side panel collapsable?
 *   Selection are area right-click? (Same menu as "Selection" in menu bar)
 * 
 * Rendering:
 *   Improve UITextInput cursor visibility
 *   Text rendering
 *     Orange text on image has yellow edges (on the left)
 *     How to handle fonts?
 *       How to handle big font sizes?
 *         Generate texture atlas using fontbm on demand?
 *         Use SDFs (either in addition to or instead of regular bitmap fonts)?
 *     Have different subdirectories for different sizes of the same font
 *     Glitchy pixels: when using Courier New (size 36), the lowecase 'u' has a
 *       diagonal line of flickering pixels going bottom-left to top-right.
 *     Text renders inconsistently: some letters are blurry and other are not.
 *       For example, using Courier New Bold with a rasterized text size of 32,
 *       the letters 'e', 'r', 'i' and 'd' are blurry, whereas 'p', 'u', 'm'
 *       and 'b' are sharp. (it seems like most blurry letters are on page 2.)
 *     Potential speedups for text rendering:
 *       Cache conversion from String to FontChar[] in TextFont
 *       Only override the parts of the text VAOs that actually change from one
 *         frame to the next
 *   Anti aliasing doesn't work despite being enabled
 *     (glfwWindowHint(GLFW_SAMPLES, 4) and glEnable(GL_MULTISAMPLE))
 *   Fix stuttering artifact when resizing windows on Linux
 *     (see https://www.glfw.org/docs/latest/window.html#window_refresh)
 *     Rename transformationMatrix to uiMatrix
 *   Maximized windows don't show up correctly on Windows 11
 *   Possible ideas for future rendering improvements:
 *     Currently, all fragment shaders are quite similarly. => Combine all
 *       fragment shaders into a single one (that gets an int containing various
 *       flags as an input)?
 *     Perhaps even combine all vertex shaders into one? (Would allow for just a
 *       single draw call, though it would probably also be a massive pain).
 *   Extras (optional):
 *     3D view
 *     Debug view
 * 
 * Backend:
 *   Sizes
 *     Move things that should not be part of sutil.ui into ui package
 *   GLFW key input: automatically recognize keyboard layout and remappings to
 *     avoid manual conversion between Y/Z and Esc/CapsLock (see Window.KEY_MAP)
 *   Performance: only ~50fps on Microsoft Surface
 *   Proper package names / structure
 *   Error handling
 * </pre>
 */
public final class MainApp extends App {

    /**
     * https://images.minitool.com/de.minitool.com/images/uploads/news/2022/02/microsoft-paint-herunterladen-installieren/microsoft-paint-herunterladen-installieren-1.png
     */
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

    public static final int NEW_DIALOG = 2, RESIZE_DIALOG = 3, NEW_COLOR_DIALOG = 4,
            SETTINGS_DIALOG = 7, CROP_DIALOG = 8, ABOUT_DIALOG = 9;

    public static final int PRIMARY_COLOR = 0, SECONDARY_COLOR = 1;
    public static final int INITIAL_PRIMARY_COLOR = SUtil.toARGB(0), INITIAL_SECONDARY_COLOR = SUtil.toARGB(255);

    public static final int MIN_IMAGE_SIZE = 1, MAX_IMAGE_SIZE = 65535;

    private static BooleanSetting transparentSelection = new BooleanSetting("transparentSelection");
    private static BooleanSetting lockSelectionRatio = new BooleanSetting("lockSelectionRatio");

    private static ColorArraySetting customUIBaseColors = new ColorArraySetting("customUIColors");

    private final ImageManager imageManager;

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

    private ImageCanvas canvas;

    public MainApp() {
        super(1280, 720, Window.MAXIMIZED, "SLPaint");

        primaryColor = INITIAL_PRIMARY_COLOR;
        secondaryColor = INITIAL_SECONDARY_COLOR;
        colorSelection = PRIMARY_COLOR;
        selectedColorPicker = new ColorPicker(getSelectedColor());
        customColorButtonArray = new ColorArray(MainUI.NUM_COLOR_BUTTONS_PER_ROW);

        imageManager = new ImageManager(this, "res/images/test.png");

        setActiveTool(ImageTool.PENCIL);
        prevTool = ImageTool.PENCIL;

        addKeyboardShortcut("new", GLFW_KEY_N, GLFW_MOD_CONTROL, this::newImage, true);
        addKeyboardShortcut("open", GLFW_KEY_O, GLFW_MOD_CONTROL, this::openImage, true);
        addKeyboardShortcut("save", GLFW_KEY_S, GLFW_MOD_CONTROL, this::saveImage, true);
        addKeyboardShortcut("save_as", GLFW_KEY_S, GLFW_MOD_CONTROL | GLFW_MOD_SHIFT, this::saveImageAs, true);
        addKeyboardShortcut("undo", GLFW_KEY_Z, GLFW_MOD_CONTROL, imageManager::undo, imageManager::canUndo);
        addKeyboardShortcut("redo", GLFW_KEY_Y, GLFW_MOD_CONTROL, imageManager::redo, imageManager::canRedo);
        addKeyboardShortcut("reset_transform", GLFW_KEY_R, 0, this::resetImageTransform, false);
        addKeyboardShortcut("zoom_in", GLFW_KEY_KP_ADD, GLFW_MOD_CONTROL, this::zoomIn, this::canZoomIn);
        addKeyboardShortcut("zoom_out", GLFW_KEY_KP_SUBTRACT, GLFW_MOD_CONTROL, this::zoomOut, this::canZoomOut);
        addKeyboardShortcut("reset_zoom", GLFW_KEY_0, GLFW_MOD_CONTROL, this::resetZoom, true);

        // all tool shortcuts
        for (ImageTool tool : ImageTool.INSTANCES) {
            tool.setApp(this);
            tool.createKeyboardShortcuts();
        }

        loadUI();
    }

    @Override
    public void update(double deltaT) {
        super.update(deltaT);

        if (colorSelection == PRIMARY_COLOR) {
            primaryColor = selectedColorPicker.getRGB();
        } else {
            secondaryColor = selectedColorPicker.getRGB();
        }

        String filename = getFilename();
        boolean hasUnsavedChanges = imageManager.hasUnsavedChanges();
        if (filename == null) {
            filename = "[Unnamed]";
            hasUnsavedChanges = false;
        }
        window.setTitle(String.format("%s%s - SLPaint", hasUnsavedChanges ? "" + (char) 0x2022 + " " : "", filename));

        // update image texture
        getImage().updateOpenGLTexture();
    }

    @Override
    public void finish() {
        super.finish();

        Settings.finish();
    }

    @Override
    protected AppUI<?> createUI() {
        return new MainUI(this);
    }

    @Override
    protected App createChildApp(int dialogType) {
        return switch (dialogType) {
            case NEW_COLOR_DIALOG -> new ColorEditorApp(this, getSelectedColor());
            case SETTINGS_DIALOG -> new SettingsApp(this);
            case CROP_DIALOG -> new ResizeApp(this, ResizeApp.CROP);
            case RESIZE_DIALOG -> new ResizeApp(this, ResizeApp.SCALE);
            case ABOUT_DIALOG -> new AboutApp(this);
            default -> null;
        };
    }

    /**
     * Stretches / squishes the image.
     * Not to be confused with {@link MainApp#cropImage(int, int)}.
     */
    public void resizeImage(int newWidth, int newHeight) {
        newWidth = Math.min(Math.max(MIN_IMAGE_SIZE, newWidth), MAX_IMAGE_SIZE);
        newHeight = Math.min(Math.max(MIN_IMAGE_SIZE, newHeight), MAX_IMAGE_SIZE);

        renderer.setTempFBOSize(newWidth, newHeight);
        renderer.resizeImage(getImage(), newWidth, newHeight);

        addImageSnapshot();
    }

    /**
     * Crops the image. Not to be confused with
     * {@link MainApp#resizeImage(int, int)}.
     */
    public void cropImage(int newWidth, int newHeight) {
        cropImage(0, 0, newWidth, newHeight);
    }

    /**
     * Crops the image. Not to be confused with
     * {@link MainApp#resizeImage(int, int)}.
     */
    public void cropImage(int x, int y, int newWidth, int newHeight) {
        newWidth = Math.min(Math.max(MIN_IMAGE_SIZE, newWidth), MAX_IMAGE_SIZE);
        newHeight = Math.min(Math.max(MIN_IMAGE_SIZE, newHeight), MAX_IMAGE_SIZE);

        getImage().crop(x, y, newWidth, newHeight, secondaryColor);
        renderer.setTempFBOSize(newWidth, newHeight);
        // If the top left corner of the image changes, its translation should change in
        // the opposite way such that the rest of the image stays in the same place.
        canvas.translateImage(new SVector(x, y));

        addImageSnapshot();
    }

    /**
     * 
     * @param degrees Must be either 90, -90 or 180.
     */
    public void rotateImage(int degrees) {
        Image image = getImage();
        switch (degrees) {
            case 90 -> image.rotateRight();
            case -90 -> image.rotateLeft();
            case 180 -> image.rotate180();
            default -> {
                final String baseStr = "Illegal image rotation angle (%d). Allowed values are 90, -90 and 180.";
                throw new IllegalArgumentException(String.format(baseStr, degrees));
            }
        }

        renderer.setTempFBOSize(image.getWidth(), image.getHeight());

        addImageSnapshot();
    }

    public void flipImageHorizontal() {
        getImage().flipHorizontal();

        addImageSnapshot();
    }

    public void flipImageVertical() {
        getImage().flipVertical();

        addImageSnapshot();
    }

    public void renderImageToImage(Image image, int x, int y, int width, int height) {
        renderer.renderImageToImage(image, x, y, width, height, getImage());
    }

    public void renderTextToImage(String text, double x, double y, double size, TextFont font) {
        renderer.renderTextToImage(text, x, y, size, toVector4f(primaryColor), font, getImage());
    }

    public void drawLine(int x0, int y0, int x1, int y1, int size, int color) {
        Image image = getImage();

        final int maxOffset = (size - 1) / 2;

        // https://iquilezles.org/articles/distfunctions2d/
        SVector ba = new SVector(x1 - x0, y1 - y0);
        double invBaSq = 1.0 / ba.magSq();

        double maxDistSq = ((double) size * size) / 4;
        int dx = Math.abs(x0 - x1);
        int dy = Math.abs(y0 - y1);
        if (dx > dy) {
            int minx = Math.min(x0, x1),
                    maxx = Math.max(x0, x1);
            for (int x = minx - maxOffset; x <= maxx + maxOffset; x++) {
                int py = x1 == x0 ? y0 : (int) Math.round(SUtil.map(x, x0, x1, y0, y1));
                for (int yoff = -2 * maxOffset; yoff <= 2 * maxOffset; yoff++) {
                    int y = py + yoff;
                    if (!image.isInside(x, y))
                        continue;
                    SVector pa = new SVector(x - x0, y - y0);
                    double h = Math.min(Math.max(pa.dot(ba) * invBaSq, 0), 1);
                    if (Double.isFinite(h)) {
                        pa.x -= ba.x * h;
                        pa.y -= ba.y * h;
                    }
                    double distSq = pa.magSq();
                    if (distSq < maxDistSq)
                        image.drawPixel(x, y, color);
                }
            }
        } else {
            int miny = Math.min(y0, y1),
                    maxy = Math.max(y0, y1);
            for (int y = miny - maxOffset; y <= maxy + maxOffset; y++) {
                int px = y1 == y0 ? x0 : (int) Math.round(SUtil.map(y, y0, y1, x0, x1));
                for (int xoff = -2 * maxOffset; xoff <= 2 * maxOffset; xoff++) {
                    int x = px + xoff;
                    if (!image.isInside(x, y))
                        continue;
                    SVector pa = new SVector(x - x0, y - y0);
                    double h = Math.min(Math.max(pa.dot(ba) * invBaSq, 0), 1);
                    if (Double.isFinite(h)) {
                        pa.x -= ba.x * h;
                        pa.y -= ba.y * h;
                    }
                    double distSq = pa.magSq();
                    if (distSq < maxDistSq)
                        image.drawPixel(x, y, color);
                }
            }
        }
    }

    public Image getImage() {
        return imageManager.getImage();
    }

    public void openImage() {
        imageManager.open();
    }

    public void newImage() {
        Image image = getImage();
        imageManager.newImage(image.getWidth(), image.getHeight());
    }

    /**
     * Gets called when Ctrl+S is pressed
     */
    public void saveImage() {
        imageManager.save();
    }

    /**
     * Gets called when Ctrl+Shift+S is pressed
     * 
     */
    public void saveImageAs() {
        imageManager.saveAs();
    }

    public void addImageSnapshot() {
        imageManager.addSnapshot();
    }

    public long getFilesize() {
        return imageManager.getFilesize();
    }

    public ImageFormat getImageFormat() {
        return imageManager.getSavedFormat();
    }

    public String getFilename() {
        return imageManager.getFilename();
    }

    public boolean isImageResizing() {
        return canvas.isImageResizing();
    }

    public int getNewImageWidth() {
        return canvas.getNewImageWidth();
    }

    public int getNewImageHeight() {
        return canvas.getNewImageHeight();
    }

    public static ColorArray getCustomUIBaseColors() {
        return customUIBaseColors.get();
    }

    public static void addCustomUIBaseColor(int color) {
        customUIBaseColors.get().addColor(color);
    }

    public void resetImageTransform() {
        canvas.resetImageTransform();
    }

    public void zoomIn() {
        canvas.zoomIn();
    }

    public boolean canZoomIn() {
        return canvas.canZoomIn();
    }

    public void zoomOut() {
        canvas.zoomOut();
    }

    public boolean canZoomOut() {
        return canvas.canZoomOut();
    }

    public void resetZoom() {
        canvas.resetZoom();
    }

    public double getImageZoom() {
        return canvas.getImageZoom();
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

    public void selectColor(int color) {
        if (colorSelection == PRIMARY_COLOR) {
            setPrimaryColor(color);
        } else {
            setSecondaryColor(color);
        }
    }

    public void setPrimaryColor(int primaryColor) {
        if (colorSelection == PRIMARY_COLOR) {
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

    public void setActiveTool(ImageTool tool) {
        if (activeTool == tool)
            return;

        // quit old tool
        if (activeTool != null) {
            activeTool.finish();
            if (tool == ImageTool.PIPETTE) {
                prevTool = activeTool;
            }
        }

        activeTool = tool;
    }

    public void switchBackToPreviousTool() {
        queueEvent(() -> setActiveTool(prevTool));
    }

    public ImageTool getActiveTool() {
        return this.activeTool;
    }

    public void addCustomColor(int color) {
        selectColor(color);
        customColorButtonArray.addColor(color);
    }

    public static boolean isTransparentSelection() {
        return transparentSelection.get();
    }

    public static void setTransparentSelection(boolean transparentSelection) {
        MainApp.transparentSelection.set(transparentSelection);
    }

    public static boolean isLockSelectionRatio() {
        return lockSelectionRatio.get();
    }

    public static void setLockSelectionRatio(boolean lockSelectionRatio) {
        MainApp.lockSelectionRatio.set(lockSelectionRatio);
    }

    public int[] getPrevMouseImagePosition() {
        return getMouseImagePosition(prevMousePos);
    }

    public int[] getMouseImagePosition() {
        return getMouseImagePosition(mousePos);
    }

    public SVector getMouseImagePosVec() {
        return getImagePosition(mousePos);
    }

    private int[] getMouseImagePosition(SVector mouse) {
        SVector mouseImagePos = getImagePosition(mouse);
        return new int[] { (int) Math.floor(mouseImagePos.x), (int) Math.floor(mouseImagePos.y) };
    }

    public SVector getImagePosition(SVector screenSpacePos) {
        return canvas.getImagePosition(screenSpacePos);
    }

    public SVector getScreenPosition(SVector imagePos) {
        return canvas.getScreenPosition(imagePos);
    }

    public SVector getMousePosition() {
        return mousePos;
    }

    public SVector getPrevMousePosition() {
        return prevMousePos;
    }

    public int[] getDisplaySize() {
        return window.getDisplaySize();
    }

    public ColorArray getCustomColorButtonArray() {
        return customColorButtonArray;
    }

    public static String formatFilesize(long filesize) {
        final String[] prefixes = { "k", "M", "G", "T" };
        long remainder = 0;
        for (int i = 0; i < prefixes.length + 1; i++) {
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

    public static int toInt(Vector4f color) {
        return SUtil.toARGB(color.x * 255, color.y * 255, color.z * 255, color.w * 255);
    }

    public static Vector4f toVector4f(Integer color) {
        if (color == null)
            return null;

        int red = SUtil.red(color);
        int green = SUtil.green(color);
        int blue = SUtil.blue(color);
        int alpha = SUtil.alpha(color);
        return (Vector4f) new Vector4f(red, green, blue, alpha).scale(1.0f / 255);
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

            if (!output.isEmpty())
                System.out.print(output);

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