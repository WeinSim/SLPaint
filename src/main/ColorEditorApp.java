package main;

import sutil.SUtil;
import sutil.math.SVector;
import sutil.ui.UIRoot;
import ui.ColorEditorUI;
import ui.components.DragTarget;
import ui.components.HueSatField;
import ui.components.LightnessScaleContainer.LightnessScale;

public class ColorEditorApp extends App {

    private MainApp mainApp;

    private HueSatField hueSatField;
    private LightnessScale lightnessScale;
    private DragTarget dragTarget;

    private int originalColor;
    private int rgb;
    private double hue;
    private double saturation;
    private double lightness;

    public ColorEditorApp(MainApp mainApp, int color) {
        super(500, 500, 0, false, "Color Editor");
        this.mainApp = mainApp;
        originalColor = color;

        setRGB(color);
    }

    @Override
    public void init() {
        super.init();

        ui = new ColorEditorUI(this);

        UIRoot root = ui.getRoot();
        SVector rootSize = root.getSize();
        int width = (int) rootSize.x;
        int height = (int) rootSize.y;
        window.setSizeAndCenter(width, height);

        dragTarget = null;
    }

    @Override
    public void update(double deltaT) {
        super.update(deltaT);

        boolean[] mouseButtons = window.getMouseButtons();
        if (!mouseButtons[0]) {
            dragTarget = null;
        }
        if (dragTarget != null) {
            dragTarget.drag();
        }
    }

    @Override
    public void finish() {
        mainApp.clearColorEditor();
    }

    public void addToCustomColors() {
        mainApp.addCustomColor(getRGB());
        window.requestClose();
    }

    public void setDragTarget(DragTarget dragTarget) {
        this.dragTarget = dragTarget;
    }

    public void setRGB(int rgb) {
        this.rgb = rgb;
        updateHSL();
    }

    public int getRGB() {
        return rgb;
    }

    private void updateRGB() {
        SVector v = SUtil.hslToRGB(hue, saturation, lightness);
        rgb = SUtil.toARGB(v.x, v.y, v.z);
    }

    public void setHue(double hue) {
        this.hue = hue;
        updateRGB();
        updateCursorPositions();
    }

    public void setSaturation(double saturation) {
        this.saturation = saturation;
        updateRGB();
        updateCursorPositions();
    }

    public void setLightness(double lightness) {
        this.lightness = lightness;
        updateRGB();
        updateCursorPositions();
    }

    public double getHue() {
        return hue;
    }

    public double getSaturation() {
        return saturation;
    }

    public double getLightness() {
        return lightness;
    }

    private void updateHSL() {
        SVector hsl = SUtil.rgbToHSL(SUtil.red(rgb), SUtil.green(rgb),
                SUtil.blue(rgb));
        hue = hsl.x;
        saturation = hsl.y;
        lightness = hsl.z;

        updateCursorPositions();
    }

    private void updateCursorPositions() {
        SVector size = hueSatField.getSize();
        hueSatField.setCursorPosition(new SVector(hue / 360 * size.x, (1 - saturation) * size.y));
        size = lightnessScale.getSize();
        lightnessScale.setCursorPosition(new SVector(0, (1 - lightness) * size.y));
    }

    public void setLightnessScale(LightnessScale lightnessScale) {
        this.lightnessScale = lightnessScale;
    }

    public void setHueSatField(HueSatField hueSatField) {
        this.hueSatField = hueSatField;
    }

    public int getOriginalColor() {
        return originalColor;
    }
}