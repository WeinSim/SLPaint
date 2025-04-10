package sutil.ui;

import java.util.function.Supplier;

import sutil.math.SVector;

public class UIText extends UIElement {

    private String text;
    private Supplier<String> textUpdater = this::getText;

    private double textSize;
    private Supplier<Double> textSizeUpdater = () -> panel.getDefaultTextSize();

    private String fontName;
    private Supplier<String> fontUpdater = () -> panel.getDefaultFontName();

    private SVector color = new SVector();
    private Supplier<SVector> colorUpdater = () -> panel.getDefaultTextColor();

    public UIText(String text) {
        this.text = text;
    }

    public UIText(Supplier<String> textUpdater) {
        this.textUpdater = textUpdater;
    }

    @Override
    public void setPreferredSize() {
        double tw = panel.textWidth(text, textSize, fontName);
        size = new SVector(tw, textSize);
    }

    @Override
    public void update() {
        text = textUpdater.get();
        textSize = textSizeUpdater.get();
        fontName = fontUpdater.get();
        color.set(colorUpdater.get());
    }

    public String getText() {
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

    public SVector getColor() {
        return color;
    }

    public void setColor(SVector color) {
        this.color.set(color);
        colorUpdater = this::getColor;
    }

    public void setColor(Supplier<SVector> colorUpdater) {
        this.colorUpdater = colorUpdater;
    }
}