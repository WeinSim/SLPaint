package main.apps;

import renderEngine.Window;
import ui.AboutUI;
import ui.AppUI;

public final class AboutApp extends App {

    public AboutApp(MainApp app) {
        super(400, 300, Window.NORMAL, false, true, "About SLPaint", app);
    }

    @Override
    protected AppUI<?> createUI() {
        return new AboutUI(this);
    }

    @Override
    protected App createChildApp(int dialogType) {
        return null;
    }
}