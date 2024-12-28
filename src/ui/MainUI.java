package ui;

import java.util.ArrayList;

import main.Image;
import main.ImageFormat;
import main.ImageTool;
import main.MainApp;
import main.SelectionManager;
import sutil.ui.UIButton;
import sutil.ui.UIContainer;
import sutil.ui.UIGetter;
import sutil.ui.UILabel;
import sutil.ui.UISeparator;
import sutil.ui.UIText;
import ui.components.ColorButton;
import ui.components.ImageCanvas;
import ui.components.ToolButton;

public class MainUI extends AppUI<MainApp> {

    public static final int COLOR_BUTTON_SIZE = 28;

    public static final int NUM_COLOR_BUTTONS_PER_ROW = 10;

    public MainUI(MainApp app) {
        super(app);

        UIContainer mainField = new UIContainer(UIContainer.VERTICAL, UIContainer.LEFT);
        mainField.zeroMargin().zeroPadding().noOutline();

        UIContainer topRow = new UIContainer(UIContainer.HORIZONTAL, UIContainer.CENTER);
        topRow.setFillSize();
        topRow.withBackground();

        UIContainer fileOptions = addTopRowSection(topRow, "File");
        fileOptions.add(new UIButton("New", () -> app.newImage()));
        fileOptions.add(new UIButton("Open", () -> app.openImage()));
        fileOptions.add(new UIButton("Save", () -> app.saveImage()));

        topRow.add(new UISeparator());

        UIContainer imageOptions = addTopRowSection(topRow, "Image");
        imageOptions.add(new UIButton("Change Size", () -> app.showDialog(MainApp.CHANGE_SIZE_DIALOG)));
        imageOptions.add(new UIButton("Rotate", () -> app.showDialog(MainApp.ROTATE_DIALOG)));
        imageOptions.add(new UIButton("Flip", () -> app.showDialog(MainApp.FLIP_DIALOG)));

        topRow.add(new UISeparator());

        UIContainer toolbox = addTopRowSection(topRow, "Tools");
        for (ImageTool tool : ImageTool.values()) {
            toolbox.add(new ToolButton(app, tool));
        }

        topRow.add(new UISeparator());

        addTopRowSection(topRow, "Size");

        topRow.add(new UISeparator());

        for (int i = 0; i < 2; i++) {
            final int index = i;
            UIContainer colorContainer = new UIContainer(UIContainer.VERTICAL, UIContainer.CENTER);
            setButtonStyle2(colorContainer, () -> app.getColorSelection() == index);
            colorContainer.setSelectable(true);

            UIGetter<Integer> cg = i == 0 ? () -> app.getPrimaryColor() : () -> app.getSecondaryColor();
            colorContainer.add(new ColorButton(cg, 36));
            UILabel label = new UILabel("%s\nColor".formatted(i == 0 ? "Primary" : "Secondary"));
            label.setAlignment(UIContainer.CENTER);
            label.zeroMargin();
            colorContainer.add(label);

            colorContainer.setClickAction(() -> app.setColorSelection(index));

            topRow.add(colorContainer);
        }

        UIContainer allColors = new UIContainer(UIContainer.VERTICAL, UIContainer.LEFT);
        allColors.zeroMargin().noOutline();
        UIContainer currentRow = null;
        for (int i = 0; i < MainApp.DEFAULT_COLORS.length; i++) {
            if (currentRow == null) {
                currentRow = new UIContainer(UIContainer.HORIZONTAL, UIContainer.CENTER);
                currentRow.zeroMargin().noOutline();
            }

            final int color = MainApp.DEFAULT_COLORS[i];
            ColorButton button = new ColorButton(() -> color, COLOR_BUTTON_SIZE);
            button.setClickAction(() -> app.selectColor(color));
            currentRow.add(button);

            if ((i + 1) % NUM_COLOR_BUTTONS_PER_ROW == 0 || i + 1 == MainApp.DEFAULT_COLORS.length) {
                allColors.add(currentRow);
                currentRow = null;
            }
        }
        CustomColorContainer ccc = new CustomColorContainer(UIContainer.HORIZONTAL, UIContainer.CENTER, app);
        ccc.zeroMargin().noOutline();
        ccc.updateColors(new ArrayList<>());
        allColors.add(ccc);
        topRow.add(allColors);

        UILabel newColor = new UILabel("+");
        setButtonStyle1(newColor);
        newColor.setClickAction(() -> app.showDialog(MainApp.NEW_COLOR_DIALOG));
        topRow.add(newColor);

        mainField.add(topRow);

        ImageCanvas canvas = new ImageCanvas(UIContainer.HORIZONTAL, UIContainer.BOTTOM, app);
        canvas.noOutline();
        UIContainer statusBar = new UIContainer(UIContainer.HORIZONTAL, UIContainer.BOTTOM);
        statusBar.zeroMargin().noOutline().withBackground();
        statusBar.add(new UILabel(() -> String.format("%.1f fps", app.getFrameRate())));
        statusBar.add(new UISeparator());
        statusBar.add(new UILabel(() -> {
            String ret = "Format: ";
            ImageFormat format = app.getImageFormat();
            if (format == null) {
                ret += "[no format], ";
            } else {
                ret += format.toString() + ", ";
            }
            String filename = app.getFilename();
            if (filename.length() > 0) {
                long filesize = app.getFilesize();
                ret += "%s (%s)".formatted(filename, MainApp.formatFilesize(filesize));
            }
            return ret;
        }));
        statusBar.add(new UISeparator());
        statusBar.add(new UILabel(() -> {
            Image image = app.getImage();
            int width = image.getWidth();
            int height = image.getHeight();
            return "Size: %d x %d px".formatted(width, height);
        }));
        statusBar.add(new UISeparator());
        statusBar.add(new UILabel(() -> {
            int[] mouseImagePos = app.getMouseImagePosition();
            boolean inside = app.getImage().isInside(mouseImagePos[0], mouseImagePos[1]);
            String ret = "Mouse Position:";
            if (inside) {
                ret += " %d, %d".formatted(mouseImagePos[0], mouseImagePos[1]);
            }
            return ret;
        }));
        statusBar.add(new UISeparator());
        statusBar.add(new UILabel(() -> {
            String ret = "Selection size:";
            SelectionManager selectionManager = app.getSelectionManager();
            if (selectionManager.getPhase() != SelectionManager.NONE) {
                ret += " %d x %d px".formatted(selectionManager.getWidth(),
                        selectionManager.getHeight());
            }
            return ret;
        }));
        canvas.add(statusBar);
        mainField.add(canvas);

        root.add(mainField);

        updateSize();
    }

    private UIContainer addTopRowSection(UIContainer topRow, String name) {
        UIContainer options = new UIContainer(UIContainer.VERTICAL, UIContainer.CENTER);
        options.zeroMargin().noOutline();
        options.setFillSize();

        UIContainer optionButtons = new UIContainer(UIContainer.HORIZONTAL, UIContainer.CENTER);
        optionButtons.zeroMargin().noOutline();
        optionButtons.setMaximalSize();

        options.add(optionButtons);
        options.add(new UIText(name));
        topRow.add(options);
        return optionButtons;
    }
}