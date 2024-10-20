package main.dialogs;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import main.ImageFormat;
import main.MainApp;

public class SaveDialog extends UserDialog {

    public SaveDialog(MainApp app) {
        super(app);
    }

    @Override
    public void run() {
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
        // app.queueEvent(() -> app.attemptSave(finalFile, finalFormat));
    }
}