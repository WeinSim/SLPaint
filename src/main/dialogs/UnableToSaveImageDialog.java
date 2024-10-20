package main.dialogs;

import javax.swing.JOptionPane;

import main.MainApp;

public class UnableToSaveImageDialog extends UserDialog {

    public UnableToSaveImageDialog(MainApp app) {
        super(app);
    }

    @Override
    public void run() {
        JOptionPane.showConfirmDialog(
                null,
                "An error occured while attempting to save the image. Please try again",
                "Unable to save image",
                JOptionPane.ERROR_MESSAGE,
                JOptionPane.OK_CANCEL_OPTION);
        app.queueEvent(() -> app.showDialog(MainApp.SAVE_DIALOG));
    }
}