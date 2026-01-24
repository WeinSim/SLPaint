package sutil.ui;

import java.util.function.Supplier;

import org.lwjglx.util.vector.Vector4f;

import sutil.math.SVector;

public class UIText extends UIElement {

    public static final Supplier<Double> NORMAL = UISizes.TEXT,
            SMALL = UISizes.TEXT_SMALL;

    private String text;
    private Supplier<String> textUpdater = this::getText;

    private double textSize;
    private Supplier<Double> textSizeUpdater;
    // private Supplier<Double> textSizeUpdater = () -> ;

    private String fontName;
    private Supplier<String> fontUpdater = UI::getDefaultFontName;

    private Vector4f color = new Vector4f();
    private Supplier<Vector4f> colorUpdater = UIColors.TEXT;

    public UIText(String text) {
        this(text, NORMAL);
    }

    public UIText(String text, Supplier<Double> textSizeUpdater) {
        this.text = text;
        this.textSizeUpdater = textSizeUpdater;
    }

    public UIText(Supplier<String> textUpdater) {
        this(textUpdater, NORMAL);
    }

    public UIText(Supplier<String> textUpdater, Supplier<Double> textSizeUpdater) {
        this.textUpdater = textUpdater;
        this.textSizeUpdater = textSizeUpdater;
    }

    public double textWidth(int len) {
        return UI.textWidth(text, textSize, fontName, len);
    }

    public int getCharIndex(double x) {
        return UI.getCharIndex(text, textSize, fontName, x);
    }

    @Override
    public void setPreferredSize() {
        size = new SVector(textWidth(text.length()), textSize);
    }

    @Override
    public void update() {
        syncText();
        textSize = textSizeUpdater.get();
        fontName = fontUpdater.get();
        color.set(colorUpdater.get());
    }

    public void syncText() {
        text = textUpdater.get();
    }

    public String getText() {
        if (text == null) {
            syncText();
        }

        return text;
    }

    public void setText(String text) {
        this.text = text;
        textUpdater = this::getText;
    }

    public void setText(Supplier<String> textUpdater) {
        this.textUpdater = textUpdater;
    }

    public String getFontName() {
        return fontName;
    }

    public void setFontName(String fontName) {
        this.fontName = fontName;
        fontUpdater = this::getFontName;
    }

    public void setFontName(Supplier<String> fontUpdater) {
        this.fontUpdater = fontUpdater;
    }

    public double getTextSize() {
        return textSize;
    }

    public void setTextSize(double textSize) {
        this.textSize = textSize;
        textSizeUpdater = this::getTextSize;
    }

    public void setTextSize(Supplier<Double> textSizeUpdater) {
        this.textSizeUpdater = textSizeUpdater;
    }

    public Vector4f getColor() {
        return color;
    }

    public void setColor(Vector4f color) {
        this.color.set(color);
        colorUpdater = this::getColor;
    }

    public void setColor(Supplier<Vector4f> colorUpdater) {
        this.colorUpdater = colorUpdater;
    }
}