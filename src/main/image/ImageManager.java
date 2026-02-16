package main.image;

import static org.lwjgl.util.nfd.NativeFileDialog.*;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.JOptionPane;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.nfd.NFDFilterItem;
import org.lwjgl.util.nfd.NFDOpenDialogArgs;
import org.lwjgl.util.nfd.NFDSaveDialogArgs;

import main.apps.MainApp;

public class ImageManager {

    public static final int RETRY = -1;

    private static final int DEFAULT_WIDTH = 1280, DEFAULT_HEIGHT = 720;

    private final MainApp app;

    private ImageHistory imageHistory;
    private ImageFile imageFile;
    private Image image;

    public ImageManager(MainApp app) {
        this.app = app;
        setEverything(null, createNewImage());
    }

    public ImageManager(MainApp app, String path) {
        this.app = app;
        try {
            ImageFile imageFile = new ImageFile(path);
            Image image = new Image(imageFile.getLoadedImage());
            setEverything(imageFile, image);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (image == null) {
            setEverything(null, createNewImage());
        }
    }

    private Image createNewImage() {
        return createNewImage(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * 
     * @implNote Has to be called from the main thread.
     */
    private Image createNewImage(int width, int height) {
        Image image = new Image(new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB));
        image.setPixels(0, 0, width, height, app.getSecondaryColor());
        return image;
    }

    /**
     * @implNote Has to be called from the main thread.
     */
    public void newImage(int width, int height) {
        (new Thread(() -> newImageImpl(width, height))).start();
    }

    private void newImageImpl(int width, int height) {
        if (checkUnsavedChanges())
            return;

        app.queueEvent(() -> setEverything(null, createNewImage(width, height)));
    }

    /**
     * @implNote Has to be called from the main thread.
     */
    public void open() {
        (new Thread(this::openImpl)).start();
    }

    private void openImpl() {
        if (checkUnsavedChanges())
            return;

        ImageFile newFile = null;
        BufferedImage bufferedImage = null;
        while (true) {
            String newPath;
            try (MemoryStack stack = MemoryStack.stackPush()) {
                PointerBuffer pathPointer = stack.mallocPointer(1);
                NFDOpenDialogArgs args = NFDOpenDialogArgs.calloc(stack)
                        .filterList(getFileFilters(stack, true))
                        .parentWindow(it -> it
                                .type(app.getWindow().getNativeHandleType())
                                .handle(app.getWindow().getNativeWindowHandle()));
                if (imageFile != null)
                    args.defaultPath(stack.UTF8(imageFile.getPath()));
                int result = NFD_OpenDialog_With(pathPointer, args);
                switch (result) {
                    case NFD_OKAY -> {
                        newPath = pathPointer.getStringUTF8(0);
                        NFD_FreePath(pathPointer.get(0));
                    }
                    case NFD_CANCEL -> {
                        return;
                    }
                    default -> {
                        System.err.format("NFD error while trying to open file: %s\n", NFD_GetError());
                        return;
                    }
                }
            }

            newFile = null;
            try {
                newFile = new ImageFile(newPath);
                bufferedImage = newFile.getLoadedImage();
                break;
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (showErrorDialog("open") == JOptionPane.OK_OPTION)
                continue;
            else
                return;
        }

        final ImageFile finalNewFile = newFile;
        final BufferedImage finalBufferedImage = bufferedImage;
        app.queueEvent(() -> setEverything(finalNewFile, new Image(finalBufferedImage)));
    }

    /**
     * @implNote Has to be called from the main thread.
     */
    public void saveAs() {
        (new Thread(this::saveAsImpl)).start();
    }

    private boolean saveAsImpl() {
        ImageFile newFile = null;
        while (true) {
            String newPath;
            try (MemoryStack stack = MemoryStack.stackPush()) {
                PointerBuffer pathPointer = stack.mallocPointer(1);
                NFDSaveDialogArgs args = NFDSaveDialogArgs.calloc(stack)
                        .filterList(getFileFilters(stack, false))
                        .parentWindow(it -> it
                                .type(app.getWindow().getNativeHandleType())
                                .handle(app.getWindow().getNativeWindowHandle()));
                if (imageFile != null) {
                    args.defaultPath(stack.UTF8(imageFile.getPath()));
                    args.defaultName(stack.UTF8(imageFile.getName()));
                }
                int result = NFD_SaveDialog_With(pathPointer, args);
                switch (result) {
                    case NFD_OKAY -> {
                        newPath = pathPointer.getStringUTF8(0);
                        NFD_FreePath(pathPointer.get(0));
                    }
                    case NFD_CANCEL -> {
                        return true;
                    }
                    default -> {
                        System.err.format("NFD error while trying to save file: %s\n", NFD_GetError());
                        return true;
                    }
                }
            }

            String suffix = newPath.substring(newPath.lastIndexOf('.') + 1);
            ImageFormat format = ImageFormat.fromString(suffix);
            if (format == null) {
                // System.err.format("Unable to detect image format. Defaulting to %s
                // instead.\n",
                // ImageFile.DEFAULT_FORMAT.toString());
                format = ImageFile.DEFAULT_FORMAT;
                newPath += "." + format.extensions[0];
            }

            newFile = null;
            try {
                newFile = new ImageFile(newPath, imageHistory.getCurrentImage(), format);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (newFile != null)
                break;

            if (showErrorDialog("save") == JOptionPane.OK_OPTION)
                continue;
            else
                return true;
        }

        final ImageFile finalNewFile = newFile;
        app.queueEvent(() -> setEverything(finalNewFile, null));
        return false;
    }

    /**
     * @implNote Has to be called from the main thread.
     */
    public void save() {
        (new Thread(imageFile == null ? this::saveAsImpl : this::saveImpl)).start();
    }

    private boolean saveImpl() {
        while (true) {
            boolean success = false;
            try {
                imageFile.saveImage(imageHistory.getCurrentImage());
                success = true;
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (success)
                break;

            if (showErrorDialog("save") == JOptionPane.OK_OPTION)
                continue;
            else
                return true;
        }
        return false;
    }

    /**
     * Notifies the user that there are unsaved changes. If "Save" is selected, the
     * current file is saved.
     * 
     * @return {@code true} if the user cancels the operation.
     * @implNote Has to be called from a separate thread.
     */
    private boolean checkUnsavedChanges() {
        if (!hasUnsavedChanges())
            return false;

        int returnCode = JOptionPane.showOptionDialog(
                null,
                "There are unsaved changes. Do you want to save them?",
                "Save changes?",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                new String[] { "Save", "Discard", "Cancel" },
                null);

        switch (returnCode) {
            case JOptionPane.CANCEL_OPTION, JOptionPane.CLOSED_OPTION -> {
                return true;
            }
            case JOptionPane.NO_OPTION -> {
                return false;
            }
            case JOptionPane.YES_OPTION -> {
                return imageFile == null ? saveAsImpl() : saveImpl();
            }
            default -> {
                System.err.format("Unknown return value from JOptionPane.showOptionDialog: %d\n", returnCode);
                return false;
            }
        }
    }

    private NFDFilterItem.Buffer getFileFilters(MemoryStack stack, boolean addAllImagesFilter) {
        ImageFormat[] formats = ImageFormat.values();
        int numFilters = formats.length + (addAllImagesFilter ? 1 : 0);
        NFDFilterItem.Buffer filters = NFDFilterItem.malloc(numFilters);
        int index = 0;
        if (addAllImagesFilter) {
            // String name = "Image Files (";
            String spec = "";
            for (ImageFormat format : formats) {
                for (String extension : format.extensions) {
                    // name += "*.%s, ".formatted(extension);
                    spec += extension + ",";
                }
            }
            // int len = name.length();
            // name = name.substring(0, len - 2);
            // name += ")";
            spec = spec.substring(0, spec.length() - 1);
            filters.get(index++)
                    .name(stack.UTF8("Image Files"))
                    .spec(stack.UTF8(spec));
        }
        for (int i = 0; index < numFilters; index++, i++) {
            filters.get(index)
                    .name(stack.UTF8(formats[i].name))
                    .spec(stack.UTF8(String.join(",", formats[i].extensions)));
        }
        return filters;
    }

    public boolean hasUnsavedChanges() {
        if (imageHistory.size() == 1)
            return false;
        if (imageFile == null)
            return true;
        return imageHistory.getCurrentImage() != imageFile.getLastSavedImage() || imageHistory.canRedo();
    }

    private static int showErrorDialog(String action) {
        String mainText = String.format(
                "An error occured while attempting to %s the image. Please try again.",
                action);
        String title = String.format("Unable to %s image", action);
        // return JOptionPane.showConfirmDialog(
        // null,
        // mainText,
        // title,
        // JOptionPane.OK_CANCEL_OPTION,
        // JOptionPane.ERROR_MESSAGE);
        return JOptionPane.showOptionDialog(
                null,
                mainText,
                title,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.ERROR_MESSAGE,
                null,
                new String[] { "Retry", "Cancel" },
                null);
    }

    private void setEverything(ImageFile imageFile, Image image) {
        this.imageFile = imageFile;

        if (image != null) {
            this.image = image;
            imageHistory = new ImageHistory(image);
        }
    }

    public void undo() {
        imageHistory.undo();
    }

    public boolean canUndo() {
        return imageHistory.canUndo();
    }

    public void redo() {
        imageHistory.redo();
    }

    public boolean canRedo() {
        return imageHistory.canRedo();
    }

    public void addSnapshot() {
        imageHistory.addSnapshot();
    }

    public long getFilesize() {
        return imageFile == null ? -1 : imageFile.getSize();
    }

    public String getFilename() {
        return imageFile == null ? null : imageFile.getName();
    }

    public ImageFormat getSavedFormat() {
        return imageFile == null ? null : imageFile.getFormat();
    }

    public Image getImage() {
        return image;
    }
}