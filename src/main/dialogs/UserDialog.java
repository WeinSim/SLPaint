package main.dialogs;

import main.apps.MainApp;

public abstract class UserDialog extends Thread {

    protected MainApp app;

    public UserDialog(MainApp app) {
        this.app = app;
    }

    @Override
    public abstract void run();
}