package main.dialogs;

import javax.swing.JOptionPane;

import main.MainApp;

public class UnimplementedDialog extends UserDialog {

    private int type;

    public UnimplementedDialog(MainApp app, int type) {
        super(app);
        this.type = type;
    }

    @Override
    public void run() {
        JOptionPane.showMessageDialog(
                null,
                String.format("Unimplemented Dialog (type = %d)", type),
                "Unimplemented Dialog",
                JOptionPane.ERROR_MESSAGE);
    }
}