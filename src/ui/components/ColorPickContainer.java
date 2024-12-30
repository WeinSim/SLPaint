package ui.components;

import main.ColorPicker;
import main.apps.MainApp;
import sutil.SUtil;
import sutil.math.SVector;
import sutil.ui.UIButton;
import sutil.ui.UIContainer;
import sutil.ui.UIGetter;
import sutil.ui.UISetter;
import sutil.ui.UIStyle;
import sutil.ui.UIText;
import sutil.ui.UITextInput;

public class ColorPickContainer extends UIContainer {

    // private static final String[] RGB_NAMES = { "Red", "Green", "Blue" };
    // private static final String[] HSL_NAMES = { "Hue", "Sat", "Light" };
    private static final String[] RGB_NAMES = { "R", "G", "B" };
    private static final String[] HSL_NAMES = { "H", "S", "L" };
    private static final double PREVIEW_WIDTH = 120, PREVIEW_HEIGHT = 80;

    public static final double DEFAULT_SIZE = 300;

    private ColorPicker colorPicker;
    private double size;

    public ColorPickContainer(ColorPicker colorPicker) {
        this(colorPicker, DEFAULT_SIZE, VERTICAL);
    }

    public ColorPickContainer(ColorPicker colorPicker, double size, int orientation) {
        super(orientation, orientation == VERTICAL ? CENTER : TOP);
        this.colorPicker = colorPicker;
        this.size = size;

        setMinimalSize();
        zeroMargin();
        noOutline();

        UIContainer row1 = createRow1();
        UIContainer row2 = createRow2();
        UIContainer row3 = createRow3();

        // row1.setOutlineNormal(true);
        // row2.setOutlineNormal(true);
        // row3.setOutlineNormal(true);

        add(row1);
        if (orientation == VERTICAL) {
            add(row2);
            add(row3);
        } else {
            UIContainer right = new UIContainer(VERTICAL, CENTER);
            right.zeroMargin().noOutline();
            right.add(row2);
            right.add(row3);
            add(right);
        }
    }

    private UIContainer createRow1() {
        UIContainer row1 = new UIContainer(UIContainer.HORIZONTAL, UIContainer.TOP);
        row1.zeroMargin().noOutline();
        HueSatField hueSatField = new HueSatField(colorPicker, size);
        row1.add(hueSatField);
        LightnessScaleContainer lsc = new LightnessScaleContainer(colorPicker);
        row1.add(lsc);
        return row1;
    }

    private UIContainer createRow2() {
        UIContainer row2 = new UIContainer(UIContainer.HORIZONTAL, UIContainer.TOP) {
            @Override
            public double getPadding() {
                return 2 * super.getPadding();
            }
        };
        row2.zeroMargin().noOutline();
        row2.setFillSize();

        UIContainer colorPreview = new UIContainer(UIContainer.VERTICAL, UIContainer.LEFT);
        colorPreview.zeroMargin().noOutline();
        UIContainer colorBox = new UIContainer(UIContainer.HORIZONTAL, 0);
        colorBox.setStyle(new UIStyle(() -> null, () -> new SVector(1, 1, 1), () -> 4.0));
        colorBox.zeroMargin().zeroPadding().noOutline();
        for (int i = 0; i < 2; i++) {
            UIContainer c = new UIContainer(0, 0);
            c.setFixedSize(new SVector(PREVIEW_WIDTH / 2, PREVIEW_HEIGHT));
            UIGetter<SVector> bgColorGetter = i == 0
                    ? () -> MainApp.toSVector(colorPicker.getInitialColor())
                    : () -> MainApp.toSVector(colorPicker.getRGB());
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
        hslInput.zeroMargin().noOutline();
        for (int i = 0; i < HSL_NAMES.length; i++) {
            UIContainer colorRow = new UIContainer(UIContainer.HORIZONTAL, UIContainer.CENTER);
            colorRow.zeroMargin().noOutline();
            colorRow.add(new UIText(HSL_NAMES[i] + ":"));
            UIGetter<String> textUpdater = switch (i) {
                case 0 -> () -> Integer.toString((int) colorPicker.getHue());
                case 1 -> () -> Integer.toString((int) (colorPicker.getSaturation() * 100));
                case 2 -> () -> Integer.toString((int) (colorPicker.getLightness() * 100));
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
                    case 0 -> colorPicker.setHue(component);
                    case 1 -> colorPicker.setSaturation(component / 100.0);
                    case 2 -> colorPicker.setLightness(component / 100.0);
                }
            };
            UITextInput colorInput = new UITextInput(textUpdater, valueUpdater);
            colorRow.add(colorInput);
            hslInput.add(colorRow);
        }
        row2.add(hslInput);

        UIContainer rgbInput = new UIContainer(UIContainer.VERTICAL, UIContainer.RIGHT);
        rgbInput.zeroMargin().noOutline();
        for (int i = 0; i < RGB_NAMES.length; i++) {
            UIContainer colorRow = new UIContainer(UIContainer.HORIZONTAL, UIContainer.CENTER);
            colorRow.zeroMargin().noOutline();
            colorRow.add(new UIText(RGB_NAMES[i] + ":"));
            UIGetter<String> textUpdater = switch (i) {
                case 0 -> () -> Integer.toString(SUtil.red(colorPicker.getRGB()));
                case 1 -> () -> Integer.toString(SUtil.green(colorPicker.getRGB()));
                case 2 -> () -> Integer.toString(SUtil.blue(colorPicker.getRGB()));
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
                int color = colorPicker.getRGB();
                int shiftAmount = 8 * (2 - j);
                int mask = 0xFF << shiftAmount;
                color &= ~mask;
                color |= component << shiftAmount;
                colorPicker.setRGB(color);
            };
            UITextInput colorInput = new UITextInput(textUpdater, valueUpdater);
            colorRow.add(colorInput);
            rgbInput.add(colorRow);
        }
        row2.add(rgbInput);
        return row2;
    }

    private UIContainer createRow3() {
        // UIContainer row3 = new UIContainer(UIContainer.VERTICAL, UIContainer.CENTER);
        // row3.noOutline();
        // row3.setFillSize();
        UIButton customColor = new UIButton("Add to Custom Colors",
                () -> colorPicker.getCloseAction().set(colorPicker.getRGB()));
        customColor.setAlignment(UIContainer.CENTER);
        customColor.setFillSize();
        return customColor;
        // row3.add(customColor);
        // return row3;
    }

    @Override
    public double getPadding() {
        return 2 * super.getPadding();
    }
}