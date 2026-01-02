package sutil.ui;

import java.util.function.Supplier;

import org.lwjglx.util.vector.Vector4f;

public class UIStyle {

    private Supplier<Vector4f> backgroundColor;
    /**
     * This is the checkerboard background that is be drawn behind the background
     */
    private CheckerboardInfo backgroundCheckerboard;

    /**
     * if stroke checkerboard is active, this field goes unused
     */
    private Supplier<Vector4f> strokeColor;
    private Supplier<Double> strokeWeight;
    /**
     * This is the checkerboard that is drawn as the outline (not behind it)
     */
    private CheckerboardInfo strokeCheckerboard;

    public UIStyle(Vector4f backgroundColor, Vector4f strokeColor, double strokeWeight) {
        this(() -> backgroundColor, () -> strokeColor, () -> strokeWeight);
    }

    public UIStyle(Supplier<Vector4f> backgroundColor, Supplier<Vector4f> strokeColor, Supplier<Double> strokeWeight) {
        this.backgroundColor = backgroundColor;
        this.strokeColor = strokeColor;
        this.strokeWeight = strokeWeight;

        setNoBackgroundCheckerboard();
        setNoStrokeCheckerboard();
    }

    public Vector4f backgroundColor() {
        return backgroundColor.get();
    }

    public boolean doBackgroundCheckerboard() {
        return backgroundCheckerboard.active().get();
    }

    public Vector4f backgroundCheckerboardColor1() {
        return backgroundCheckerboard.color1().get();
    }

    public Vector4f backgroundCheckerboardColor2() {
        return backgroundCheckerboard.color2().get();
    }

    public double backgroundCheckerboardSize() {
        return backgroundCheckerboard.size().get();
    }

    public Vector4f strokeColor() {
        return strokeColor.get();
    }

    public double strokeWeight() {
        return strokeWeight.get();
    }

    public boolean doStrokeCheckerboard() {
        return strokeCheckerboard.active().get();
    }

    public Vector4f strokeCheckerboardColor1() {
        return strokeCheckerboard.color1().get();
    }

    public Vector4f strokeCheckerboardColor2() {
        return strokeCheckerboard.color2().get();
    }

    public double strokeCheckerboardSize() {
        return strokeCheckerboard.size().get();
    }

    public void setBackgroundColor(Vector4f backgroundColor) {
        setBackgroundColor(() -> backgroundColor);
    }

    public void setBackgroundColor(Supplier<Vector4f> backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public void setBackgroundCheckerboard(Vector4f color1, Vector4f color2, double size) {
        setBackgroundCheckerboard(() -> color1, () -> color2, () -> size);
    }

    public void setBackgroundCheckerboard(Supplier<Vector4f> color1, Supplier<Vector4f> color2, Supplier<Double> size) {
        setBackgroundCheckerboard(() -> true, color1, color2, size);
    }

    public void setBackgroundCheckerboard(Supplier<Boolean> active, Supplier<Vector4f> color1, Supplier<Vector4f> color2,
            Supplier<Double> size) {

        backgroundCheckerboard = new CheckerboardInfo(active, color1, color2, size);
    }

    public void setNoBackgroundCheckerboard() {
        setBackgroundCheckerboard(() -> false, () -> new Vector4f(), () -> new Vector4f(), () -> 1.0);
    }

    public void setStrokeCheckerboard(Vector4f color1, Vector4f color2, double size) {
        setStrokeCheckerboard(() -> color1, () -> color2, () -> size);
    }

    public void setStrokeCheckerboard(Supplier<Vector4f> color1, Supplier<Vector4f> color2, Supplier<Double> size) {
        setStrokeCheckerboard(() -> true, color1, color2, size);
    }

    public void setStrokeCheckerboard(Supplier<Boolean> active, Supplier<Vector4f> color1, Supplier<Vector4f> color2,
            Supplier<Double> size) {

        strokeCheckerboard = new CheckerboardInfo(active, color1, color2, size);
    }

    public void setNoStrokeCheckerboard() {
        setStrokeCheckerboard(() -> false, () -> new Vector4f(), () -> new Vector4f(), () -> 1.0);
    }

    public void setStrokeColor(Vector4f strokeColor) {
        setStrokeColor(() -> strokeColor);
    }

    public void setStrokeColor(Supplier<Vector4f> strokeColor) {
        this.strokeColor = strokeColor;
    }

    public void setStrokeWeight(double strokeWeight) {
        setStrokeWeight(() -> strokeWeight);
    }

    public void setStrokeWeight(Supplier<Double> strokeWeight) {
        this.strokeWeight = strokeWeight;
    }

    private record CheckerboardInfo(Supplier<Boolean> active, Supplier<Vector4f> color1, Supplier<Vector4f> color2,
            Supplier<Double> size) {

    }
}