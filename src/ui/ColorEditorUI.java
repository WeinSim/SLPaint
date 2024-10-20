package ui;

import main.ColorEditorApp;
import main.MainApp;
import sutil.SUtil;
import sutil.math.SVector;
import sutil.ui.UIButton;
import sutil.ui.UIContainer;
import sutil.ui.UIGetter;
import sutil.ui.UILabel;
import sutil.ui.UISetter;
import sutil.ui.UIStyle;
import sutil.ui.UIText;
import sutil.ui.UITextInput;
import ui.components.HueSatField;
import ui.components.LightnessScaleContainer;

public class ColorEditorUI extends AppUI<ColorEditorApp> {

    // private static final String[] RGB_NAMES = { "Red", "Green", "Blue" };
    // private static final String[] HSL_NAMES = { "Hue", "Sat", "Light" };
    private static final String[] RGB_NAMES = { "R", "G", "B" };
    private static final String[] HSL_NAMES = { "H", "S", "L" };
    private static final double PREVIEW_WIDTH = 120, PREVIEW_HEIGHT = 80;

    public ColorEditorUI(ColorEditorApp app) {
        super(app);

        root.setOrientation(UIContainer.VERTICAL);
        root.setAlignment(UIContainer.CENTER);
        root.setMinimalSize();
        // root.setZeroPadding(false);

        UIContainer row1 = new UIContainer(UIContainer.HORIZONTAL, UIContainer.TOP);
        row1.noOutline();
        HueSatField hueSatField = new HueSatField(app);
        row1.add(hueSatField);
        row1.add(new LightnessScaleContainer(app));
        root.add(row1);

        UIContainer row2 = new UIContainer(UIContainer.HORIZONTAL, UIContainer.TOP);
        row2.zeroMargin().noOutline();
        row2.setFillSize();
        UIContainer colorPreview = new UIContainer(UIContainer.VERTICAL, UIContainer.LEFT);
        colorPreview.noOutline();
        UIContainer colorBox = new UIContainer(UIContainer.HORIZONTAL, 0);
        colorBox.setStyle(new UIStyle(() -> null, () -> new SVector(1, 1, 1), () -> 4.0));
        colorBox.zeroMargin().zeroPadding().noOutline();
        for (int i = 0; i < 2; i++) {
            UIContainer c = new UIContainer(0, 0);
            c.setFixedSize(new SVector(PREVIEW_WIDTH / 2, PREVIEW_HEIGHT));
            UIGetter<SVector> bgColorGetter = i == 0
                    ? () -> MainApp.toSVector(app.getOriginalColor())
                    : () -> MainApp.toSVector(app.getRGB());
            c.setStyle(new UIStyle(bgColorGetter, () -> null, () -> 1.0));
            colorBox.add(c);
        }
        colorPreview.add(colorBox);
        colorPreview.add(new UIText("Preview"));
        row2.add(colorPreview);

        UIContainer gap = new UIContainer(0, 0);
        gap.zeroMargin().noOutline();
        gap.setMaximalSize();
        row2.add(gap);

        UIContainer hslInput = new UIContainer(UIContainer.VERTICAL, UIContainer.RIGHT);
        hslInput.noOutline();
        for (int i = 0; i < HSL_NAMES.length; i++) {
            UIContainer colorRow = new UIContainer(UIContainer.HORIZONTAL, UIContainer.CENTER);
            colorRow.zeroMargin().noOutline();
            colorRow.add(new UIText(HSL_NAMES[i] + ":"));
            UIGetter<String> textUpdater = switch (i) {
                case 0 -> () -> Integer.toString((int) app.getHue());
                case 1 -> () -> Integer.toString((int) (app.getSaturation() * 100));
                case 2 -> () -> Integer.toString((int) (app.getLightness() * 100));
                default -> null;
            };
            final int j = i;
            UISetter<String> valueUpdater = (String s) -> {
                int component = 0;
                if (s.length() > 0) {
                    try {
                        component = Integer.parseInt(s);
                    } catch (NumberFormatException e) {
                        return;
                    }
                }
                int max = j == 0 ? 360 : 100;
                component = Math.min(Math.max(0, component), max);
                switch (j) {
                    case 0 -> app.setHue(component);
                    case 1 -> app.setSaturation(component / 100.0);
                    case 2 -> app.setLightness(component / 100.0);
                }
            };
            UITextInput colorInput = new UITextInput(textUpdater, valueUpdater);
            colorRow.add(colorInput);
            hslInput.add(colorRow);
        }
        row2.add(hslInput);

        UIContainer rgbInput = new UIContainer(UIContainer.VERTICAL, UIContainer.RIGHT);
        rgbInput.noOutline();
        for (int i = 0; i < RGB_NAMES.length; i++) {
            UIContainer colorRow = new UIContainer(UIContainer.HORIZONTAL, UIContainer.CENTER);
            colorRow.zeroMargin().noOutline();
            colorRow.add(new UIText(RGB_NAMES[i] + ":"));
            UIGetter<String> textUpdater = switch (i) {
                case 0 -> () -> Integer.toString(SUtil.red(app.getRGB()));
                case 1 -> () -> Integer.toString(SUtil.green(app.getRGB()));
                case 2 -> () -> Integer.toString(SUtil.blue(app.getRGB()));
                default -> null;
            };
            final int j = i;
            UISetter<String> valueUpdater = (String s) -> {
                int component = 0;
                if (s.length() > 0) {
                    try {
                        component = Integer.parseInt(s);
                    } catch (NumberFormatException e) {
                        return;
                    }
                }
                component = Math.min(Math.max(0, component), 255);
                int color = app.getRGB();
                int shiftAmount = 8 * (2 - j);
                int mask = 0xFF << shiftAmount;
                color &= ~mask;
                color |= component << shiftAmount;
                app.setRGB(color);
            };
            UITextInput colorInput = new UITextInput(textUpdater, valueUpdater);
            colorRow.add(colorInput);
            rgbInput.add(colorRow);
        }
        row2.add(rgbInput);
        root.add(row2);

        UIContainer row3 = new UIContainer(UIContainer.VERTICAL, UIContainer.CENTER);
        row3.noOutline();
        row3.setFillSize();
        UILabel customColor = new UIButton("Add to Custom Colors", () -> app.addToCustomColors());
        customColor.setAlignment(UIContainer.CENTER);
        customColor.setFillSize();
        row3.add(customColor);
        root.add(row3);

        updateSize();
    }
}