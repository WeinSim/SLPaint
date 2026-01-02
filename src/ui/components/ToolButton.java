package ui.components;

import main.apps.MainApp;
import main.tools.ImageTool;
import sutil.ui.UIButton;
import ui.AppUI;

public class ToolButton extends UIButton {

    public ToolButton(MainApp app, ImageTool tool) {
        super(tool.getName().charAt(0) + "", () -> app.setActiveTool(tool));

        AppUI.setSelectableButtonStyle(this, () -> app.getActiveTool() == tool);
    }
}