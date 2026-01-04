package ui.components;

import java.util.function.Consumer;
import java.util.function.Supplier;

import main.ColorPicker;
import main.apps.App;
import main.apps.MainApp;
import sutil.SUtil;
import sutil.math.SVector;
import sutil.ui.UIButton;
import sutil.ui.UIContainer;
import sutil.ui.UINumberInput;
import sutil.ui.UIScale;
import sutil.ui.UIStyle;
import sutil.ui.UIText;
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

    public ColorPickContainer(ColorPicker colorPicker, Consumer<Integer> buttonAction) {
        this(colorPicker, buttonAction, Sizes.COLOR_PICKER_EXTRA_WINDOW.size, VERTICAL, true, true);
    }

    public ColorPickContainer(ColorPicker colorPicker, Consumer<Integer> buttonAction, double size, int orientation,
            boolean addAlpha, boolean addPreview) {

        super(orientation, orientation == VERTICAL ? CENTER : TOP);
        this.colorPicker = colorPicker;
        this.size = size;

        zeroMargin().noOutline();

        paddingScale = 2;

        UIContainer row1 = createRow1();
        UIContainer row2 = addAlpha ? createRow2() : null;
        UIContainer row3 = createRow3(addPreview);
        UIContainer row4 = createRow4(buttonAction);

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
        row1.setPaddingScale(2);
        row1.zeroMargin().noOutline();
        HueSatField hueSatField = new HueSatField(colorPicker, size);
        row1.add(hueSatField);
        UIScale lightnessScale = new LightnessScale(UIContainer.VERTICAL, colorPicker);
        row1.add(lightnessScale);
        return row1;
    }

    private UIContainer createRow2() {
        UIContainer row2 = new UIContainer(UIContainer.HORIZONTAL, UIContainer.CENTER);
        row2.zeroMargin().noOutline();
        row2.setHFillSize();

        row2.add(new UIText("Alpha:"));

        Supplier<Integer> getter = () -> SUtil.alpha(colorPicker.getRGB());
        Consumer<Integer> setter = alpha -> colorPicker.setAlpha(alpha);
        UINumberInput alphaInput = new UINumberInput(getter, setter);
        row2.add(alphaInput);

        UIScale alphaScale = new AlphaScale(UIContainer.HORIZONTAL, colorPicker);
        row2.add(alphaScale);
        return row2;
    }

    private UIContainer createRow3(boolean addPreview) {
        UIContainer row3 = new UIContainer(UIContainer.HORIZONTAL, UIContainer.TOP);
        row3.zeroMargin().noOutline();
        row3.setHFillSize();

        UIContainer colorPreview = new UIContainer(UIContainer.VERTICAL, UIContainer.LEFT);
        colorPreview.zeroMargin().noOutline();
        UIContainer colorBox = new UIContainer(UIContainer.HORIZONTAL, 0);
        colorBox.setStyle(new UIStyle(() -> null, Colors::text, () -> 2.0));
        colorBox.zeroMargin().zeroPadding().noOutline();
        double previewWidth = Sizes.COLOR_PICKER_PREVIEW.width,
                previewHeight = Sizes.COLOR_PICKER_PREVIEW.height;
        if (addPreview) {
            previewWidth /= 2;
        }
        for (int i = addPreview ? 0 : 1; i < 2; i++) {
            Supplier<Integer> bgColorSupplier = i == 0
                    ? colorPicker::getInitialColor
                    : colorPicker::getRGB;
            colorBox.add(new UIColorElement(bgColorSupplier, new SVector(previewWidth, previewHeight), false));
        }
        colorPreview.add(colorBox);
        colorPreview.add(new UIText("Preview"));
        row3.add(colorPreview);

        UIContainer gap = new UIContainer(0, 0);
        gap.zeroMargin().noOutline();
        gap.setHFillSize();
        row3.add(gap);

        UIContainer hslInput = new UIContainer(UIContainer.VERTICAL, UIContainer.RIGHT);
        hslInput.zeroMargin().noOutline();
        hslInput.setVisibilitySupplier(App::isHSLColorSpace);
        for (int i = 0; i < HSL_NAMES.length; i++) {
            UIContainer colorRow = new UIContainer(UIContainer.HORIZONTAL, UIContainer.CENTER);
            colorRow.zeroMargin().noOutline();
            colorRow.add(new UIText(HSL_NAMES[i] + ":"));
            Supplier<Integer> getter = switch (i) {
                case 0 -> () -> (int) colorPicker.getHue();
                case 1 -> () -> (int) (colorPicker.getHSLSaturation() * 100);
                case 2 -> () -> (int) (colorPicker.getLightness() * 100);
                default -> null;
            };
            Consumer<Integer> setter = switch (i) {
                case 0 -> hue -> colorPicker.setHSLHue(Math.min(Math.max(hue, 0), 359));
                case 1 -> saturation -> colorPicker.setHSLSaturation(saturation / 100.0);
                case 2 -> lightness -> colorPicker.setLightness(lightness / 100.0);
                default -> null;
            };
            UINumberInput colorInput = new UINumberInput(getter, setter);
            colorRow.add(colorInput);
            hslInput.add(colorRow);
        }
        row3.add(hslInput);

        UIContainer hsvInput = new UIContainer(UIContainer.VERTICAL, UIContainer.RIGHT);
        hsvInput.zeroMargin().noOutline();
        hsvInput.setVisibilitySupplier(() -> !MainApp.isHSLColorSpace());
        for (int i = 0; i < HSV_NAMES.length; i++) {
            UIContainer colorRow = new UIContainer(UIContainer.HORIZONTAL, UIContainer.CENTER);
            colorRow.zeroMargin().noOutline();
            colorRow.add(new UIText(HSV_NAMES[i] + ":"));
            Supplier<Integer> getter = switch (i) {
                case 0 -> () -> (int) colorPicker.getHue();
                case 1 -> () -> (int) (colorPicker.getHSVSaturation() * 100);
                case 2 -> () -> (int) (colorPicker.getValue() * 100);
                default -> null;
            };
            Consumer<Integer> setter = switch (i) {
                case 0 -> hue -> colorPicker.setHSVHue(Math.min(Math.max(hue, 0), 360));
                case 1 -> saturation -> colorPicker.setHSVSaturation(saturation / 100.0);
                case 2 -> value -> colorPicker.setValue(value / 100.0);
                default -> null;
            };
            UINumberInput colorInput = new UINumberInput(getter, setter);
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
            Supplier<Integer> getter = switch (i) {
                case 0 -> () -> SUtil.red(colorPicker.getRGB());
                case 1 -> () -> SUtil.green(colorPicker.getRGB());
                case 2 -> () -> SUtil.blue(colorPicker.getRGB());
                default -> null;
            };
            final int j = i;
            Consumer<Integer> setter = component -> {
                component = Math.min(Math.max(component, 0), 255);
                int color = colorPicker.getRGB();
                int shiftAmount = 8 * (2 - j);
                int mask = 0xFF << shiftAmount;
                color &= ~mask;
                color |= component << shiftAmount;
                colorPicker.setRGB(color);
            };
            UINumberInput colorInput = new UINumberInput(getter, setter);
            colorRow.add(colorInput);
            rgbInput.add(colorRow);
        }
        row3.add(rgbInput);

        return row3;
    }

    private UIContainer createRow4(Consumer<Integer> buttonAction) {
        UIButton customColor = new UIButton("Add to Custom Colors", () -> buttonAction.accept(colorPicker.getRGB()));
        customColor.setAlignment(UIContainer.CENTER);
        customColor.setHFillSize();
        return customColor;
    }
}