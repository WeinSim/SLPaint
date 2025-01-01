package main;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import main.apps.MainApp;

public class ImageFileManager {

    private static final FileFilter ALL_IMAGE_FORMATS_FILTER = generateAllImageFormatsFilter();

    private final MainApp app;

    private ImageFile imageFile;
    private Image image;

    public ImageFileManager(MainApp app) {
        this.app = app;
        image = createNewImage(1280, 720);
    }

    public ImageFileManager(MainApp app, String path) {
        this.app = app;
        open(new File(path));
    }

    private Image createNewImage(int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Image image = new Image(img);
        image.setPixels(0, 0, width, height, app.getSecondaryColor());
        return image;
    }

    /**
     * @implNote Has to be called from the main thread.
     */
    public void newImage(int width, int height) {
        (new Thread(() -> {
            if (checkUnsavedChanges()) {
                return;
            }

            app.queueEvent(() -> {
                image = createNewImage(image.getWidth(), image.getHeight());
                imageFile = null;
            });
        })).start();
    }

    /**
     * Notifies the user that there are unsaved changes. If "Save" is selected, the
     * current file is saved.
     * 
     * @return {@code true} if either CANCEL_OPTION or CLOSED_OPTION are returned
     *         from {@code JOptionPane.showOptionDialog()}, {@code false} otherwise
     * @implNote Has to be called from a separate thread.
     */
    private boolean checkUnsavedChanges() {
        if (!hasUnsavedChanges()) {
            return false;
        }
        int returnCode = JOptionPane.showOptionDialog(
                null,
                "There are unsaved changes. Do you want to save them? ",
                "Save changes?",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                new String[] { "Save", "Discard", "Cancel" },
                null);
        if (returnCode == JOptionPane.YES_OPTION) {
            app.queueEvent(() -> save());
        } else if (returnCode != JOptionPane.NO_OPTION) {
            // could be CANCEL_OPTION or CLOSE_OPTION
            return true;
        }
        return false;
    }

    /**
     * @implNote Has to be called from the main thread.
     */
    public void open() {
        (new Thread(() -> {
            if (checkUnsavedChanges()) {
                return;
            }
            showOpenDialog(imageFile == null ? null : imageFile.getFile());
        })).start();
    }

    /**
     * @implNote Has to be called from a separate thread.
     */
    private void showOpenDialog(File startDirectory) {
        JFileChooser fc = new JFileChooser();
        if (startDirectory != null) {
            fc.setCurrentDirectory(startDirectory);
        }
        fc.addChoosableFileFilter(ALL_IMAGE_FORMATS_FILTER);
        for (ImageFormat format : ImageFormat.values()) {
            fc.addChoosableFileFilter(format.fileFilter);
        }
        fc.setFileFilter(ALL_IMAGE_FORMATS_FILTER);
        int returnState = fc.showOpenDialog(fc);
        if (returnState == JFileChooser.APPROVE_OPTION) {
            app.queueEvent(() -> open(fc.getSelectedFile()));
        }
    }

    /**
     * @implNote Has to be called from the main thread.
     */
    private void open(File file) {
        ImageFile newImageFile = null;
        // TODO
        // edge case: user tries to open the file that is already open
        if (imageFile != null) { // && imageFile.getFile().equals(file)) {
            // avoid OverlappingFileLockException
            imageFile.close();
        }
        try {
            newImageFile = new ImageFile(file);
        } catch (IOException e) {
        }
        if (newImageFile == null) {
            if (image == null) {
                image = createNewImage(1280, 720);
            }
            int returnCode = showErrorDialog("load");
            if (returnCode == JOptionPane.OK_OPTION) {
                (new Thread(() -> showOpenDialog(file))).start();
            }
        } else {
            if (imageFile != null) {
                // imageFile.close();
            }
            if (image != null) {
                image.cleanUp();
            }
            imageFile = newImageFile;
            image = newImageFile.getImage();
        }
    }

    /**
     * @implNote Has to be called from the main thread.
     */
    public void save() {
        save(false, true);
    }

    /**
     * @implNote Has to be called from the main thread.
     */
    public void saveAs() {
        save(true, true);
    }

    private void save(boolean forceNewFile, boolean newThread) {
        Runnable saveLogic = () -> {
            if (imageFile == null || forceNewFile) {
                saveNewFile();
            } else {
                saveCurrentFile();
            }
        };
        if (newThread) {
            (new Thread(saveLogic)).start();
        } else {
            saveLogic.run();
        }
    }

    private void saveCurrentFile() {
        boolean success = false;
        while (true) {
            try {
                success = imageFile.save();
            } catch (IOException e) {
            }
            if (success) {
                break;
            }
            int returnCode = showErrorDialog("save");
            if (returnCode != JOptionPane.OK_OPTION) {
                break;
            }
        }
    }

    private void saveNewFile() {
        JFileChooser fc = new JFileChooser();
        for (FileFilter filter : fc.getChoosableFileFilters()) {
            fc.removeChoosableFileFilter(filter);
        }
        for (ImageFormat format : ImageFormat.values()) {
            fc.addChoosableFileFilter(format.fileFilter);
        }
        File selectedFile;
        do {
            int returnState = fc.showSaveDialog(fc);
            if (returnState != JFileChooser.APPROVE_OPTION) {
                return;
            }
            selectedFile = fc.getSelectedFile();
            if (selectedFile.exists()) {
                int overwrite = JOptionPane.showConfirmDialog(
                        fc,
                        String.format(
                                "The specified file (%s) already exists. Do you want to overwrite it?",
                                selectedFile.getAbsolutePath()),
                        "File already exists",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.INFORMATION_MESSAGE);
                if (overwrite == JOptionPane.NO_OPTION) {
                    selectedFile = null;
                }
            }
        } while (selectedFile == null);
        ImageFormat format = ImageFormat.fromFileFilter(fc.getFileFilter());
        if (format == null) {
            // should never happen
            System.out.print("Invalid FileFilter selected. ");
            System.out.print("Unable to find corresponding ImageFormat! ");
            System.out.println("(defaulting to PNG instead)");
            format = ImageFormat.PNG;
        }
        final File finalFile = selectedFile;
        final ImageFormat finalFormat = format;
        try {
            imageFile = new ImageFile(image, finalFile, finalFormat);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        // app.queueEvent(() -> app.attemptSave(finalFile, finalFormat));
    }

    private boolean hasUnsavedChanges() {
        return true;
    }

    private static int showErrorDialog(String action) {
        String mainText = String.format(
                "An error occured while attempting to %s the image. Please try again.",
                action);
        String title = String.format("Unable to %s image", action);
        return JOptionPane.showConfirmDialog(
                null,
                mainText,
                title,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.ERROR_MESSAGE);
    }

    /**
     * @return The current image, which is never {@code null}.
     */
    public Image getImage() {
        return image;
    }

    /**
     * @return The {@code ImageFule} that is currently open. If the current image is
     *         not associated with any file, {@code null} is returned.
     */
    public ImageFile getImageFile() {
        return imageFile;
    }

    private static FileFilter generateAllImageFormatsFilter() {
        ArrayList<String> allExtensions = new ArrayList<>();
        String generalDescription = "Image Files (";
        for (ImageFormat format : ImageFormat.values()) {
            for (int i = 0; i < format.extensions.length; i++) {
                String extension = format.extensions[i];
                allExtensions.add(extension);
                generalDescription += "*.%s, ".formatted(extension);
            }
        }

        int len = generalDescription.length();
        generalDescription = generalDescription.substring(0, len - 2);
        generalDescription += ")";
        String[] array = new String[allExtensions.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = allExtensions.get(i);
        }

        return new FileNameExtensionFilter(generalDescription, array);
    }
}