package ui.components;

import main.ColorPicker;
import sutil.SUtil;
import sutil.math.SVector;
import sutil.ui.UIButton;
import sutil.ui.UIContainer;
import sutil.ui.UIGetter;
import sutil.ui.UISetter;
import sutil.ui.UIStyle;
import sutil.ui.UIText;
import sutil.ui.UITextInput;
import ui.Colors;
import ui.Sizes;

public class ColorPickContainer extends UIContainer {

    // private static final String[] RGB_NAMES = { "Red", "Green", "Blue" };
    // private static final String[] HSL_NAMES = { "Hue", "Sat", "Light" };
    private static final String[] RGB_NAMES = { "R", "G", "B" };
    private static final String[] HSL_NAMES = { "H", "S", "L" };
    private static final String[] HSV_NAMES = { "H", "S", "V" };

    private ColorPicker colorPicker;
    private double size;

    public ColorPickContainer(ColorPicker colorPicker) {
        this(colorPicker, Sizes.COLOR_PICKER_EXTRA_WINDOW.size, VERTICAL, true, true);
    }

    public ColorPickContainer(ColorPicker colorPicker, double size, int orientation, boolean addAlpha,
            boolean addPreview) {
        super(orientation, orientation == VERTICAL ? CENTER : TOP);
        this.colorPicker = colorPicker;
        this.size = size;

        setMinimalSize();
        zeroMargin();
        noOutline();

        UIContainer row1 = createRow1();
        UIContainer row2 = addAlpha ? createRow2() : null;
        UIContainer row3 = createRow3(addPreview);
        UIContainer row4 = createRow4();

        // row1.setOutlineNormal(true);
        // row2.setOutlineNormal(true);
        // row3.setOutlineNormal(true);

        add(row1);
        if (orientation == VERTICAL) {
            if (addAlpha) {
                add(row2);
            }
            add(row3);
            add(row4);
        } else {
            UIContainer right = new UIContainer(VERTICAL, CENTER);
            right.zeroMargin().noOutline();
            if (addAlpha) {
                right.add(row2);
            }
            right.add(row3);
            right.add(row4);
            add(right);
        }
    }

    private UIContainer createRow1() {
        UIContainer row1 = new UIContainer(UIContainer.HORIZONTAL, UIContainer.TOP);
        row1.zeroMargin().noOutline();
        HueSatField hueSatField = new HueSatField(colorPicker, size);
        row1.add(hueSatField);
        UIScaleContainer lightnessScale = new UIScaleContainer(new LightnessScale(UIContainer.VERTICAL, colorPicker));
        row1.add(lightnessScale);
        return row1;
    }

    private UIContainer createRow2() {
        UIContainer row2 = new UIContainer(UIContainer.HORIZONTAL, UIContainer.CENTER);
        row2.zeroMargin().noOutline();
        row2.setFillSize();

        row2.add(new UIText("Alpha:"));

        UIGetter<String> textUpdater = () -> Integer.toString(SUtil.alpha(colorPicker.getRGB()));
        UISetter<String> valueUpdater = (String s) -> {
            int alpha = 0;
            if (s.length() > 0) {
                try {
                    alpha = Integer.parseInt(s);
                } catch (NumberFormatException e) {
                    return;
                }
            }
            alpha = Math.min(Math.max(0, alpha), 255);
            colorPicker.setAlpha(alpha);
        };
        UITextInput alphaInput = new UITextInput(textUpdater, valueUpdater);
        row2.add(alphaInput);

        UIScaleContainer alphaScale = new UIScaleContainer(new AlphaScale(UIContainer.HORIZONTAL, colorPicker));
        alphaScale.setMaximalSize();
        row2.add(alphaScale);
        return row2;
    }

    private UIContainer createRow3(boolean addPreview) {
        UIContainer row3 = new UIContainer(UIContainer.HORIZONTAL, UIContainer.TOP) {
            @Override
            public double getPadding() {
                return 2 * super.getPadding();
            }
        };
        row3.zeroMargin().noOutline();
        row3.setFillSize();

        // if (addPreview) {
        UIContainer colorPreview = new UIContainer(UIContainer.VERTICAL, UIContainer.LEFT);
        colorPreview.zeroMargin().noOutline();
        UIContainer colorBox = new UIContainer(UIContainer.HORIZONTAL, 0);
        colorBox.setStyle(new UIStyle(() -> null, () -> Colors.getTextColor(), () -> 2.0));
        colorBox.zeroMargin().zeroPadding().noOutline();
        double previewWidth = Sizes.COLOR_PICKER_PREVIEW.width,
                previewHeight = Sizes.COLOR_PICKER_PREVIEW.height;
        if (addPreview) {
            previewWidth /= 2;
        }
        // double width = addPreview ? previewWidth / 2 : previewWidth;
        for (int i = addPreview ? 0 : 1; i < 2; i++) {
            UIGetter<Integer> bgColorGetter = i == 0
                    ? () -> colorPicker.getInitialColor()
                    : () -> colorPicker.getRGB();
            colorBox.add(new UIColorElement(bgColorGetter, new SVector(previewWidth, previewHeight), false));
        }
        colorPreview.add(colorBox);
        colorPreview.add(new UIText("Preview"));
        row3.add(colorPreview);

        UIContainer gap = new UIContainer(0, 0);
        gap.zeroMargin().noOutline();
        gap.setMaximalSize();
        row3.add(gap);
        // }

        UIContainer hslInput = new UIContainer(UIContainer.VERTICAL, UIContainer.RIGHT);
        hslInput.zeroMargin().noOutline();
        for (int i = 0; i < HSL_NAMES.length; i++) {
            UIContainer colorRow = new UIContainer(UIContainer.HORIZONTAL, UIContainer.CENTER);
            colorRow.zeroMargin().noOutline();
            colorRow.add(new UIText(HSL_NAMES[i] + ":"));
            UIGetter<String> textUpdater = switch (i) {
                case 0 -> () -> Integer.toString((int) colorPicker.getHue());
                case 1 -> () -> Integer.toString((int) (colorPicker.getHSLSaturation() * 100));
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
                    case 0 -> colorPicker.setHSLHue(component);
                    case 1 -> colorPicker.setHSLSaturation(component / 100.0);
                    case 2 -> colorPicker.setLightness(component / 100.0);
                }
            };
            UITextInput colorInput = new UITextInput(textUpdater, valueUpdater);
            colorRow.add(colorInput);
            hslInput.add(colorRow);
        }
        row3.add(hslInput);

        UIContainer hsvInput = new UIContainer(UIContainer.VERTICAL, UIContainer.RIGHT);
        hsvInput.zeroMargin().noOutline();
        for (int i = 0; i < HSV_NAMES.length; i++) {
            UIContainer colorRow = new UIContainer(UIContainer.HORIZONTAL, UIContainer.CENTER);
            colorRow.zeroMargin().noOutline();
            colorRow.add(new UIText(HSV_NAMES[i] + ":"));
            UIGetter<String> textUpdater = switch (i) {
                case 0 -> () -> Integer.toString((int) colorPicker.getHue());
                case 1 -> () -> Integer.toString((int) (colorPicker.getHSVSaturation() * 100));
                case 2 -> () -> Integer.toString((int) (colorPicker.getValue() * 100));
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
                    case 0 -> colorPicker.setHSVHue(component);
                    case 1 -> colorPicker.setHSVSaturation(component / 100.0);
                    case 2 -> colorPicker.setValue(component / 100.0);
                }
            };
            UITextInput colorInput = new UITextInput(textUpdater, valueUpdater);
            colorRow.add(colorInput);
            hsvInput.add(colorRow);
        }
        row3.add(hsvInput);

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
        row3.add(rgbInput);

        return row3;
    }

    private UIContainer createRow4() {
        UIButton customColor = new UIButton("Add to Custom Colors",
                () -> colorPicker.getCloseAction().set(colorPicker.getRGB()));
        customColor.setAlignment(UIContainer.CENTER);
        customColor.setFillSize();
        return customColor;
    }

    @Override
    public double getPadding() {
        return 2 * super.getPadding();
    }
}