package ui;

import java.util.function.Supplier;

import main.apps.App;
import renderEngine.fonts.TextFont;
import sutil.math.SVector;
import sutil.ui.UIContainer;
import sutil.ui.UIElement;
import sutil.ui.UIPanel;
import sutil.ui.UIRoot;
import sutil.ui.UIStyle;
import sutil.ui.UITextInput;

public abstract class AppUI<T extends App> extends UIPanel {

    protected T app;

    public AppUI(T app) {
        this.app = app;

        margin = Sizes.MARGIN.size;
        padding = Sizes.PADDING.size;

        defaultTextSize = Sizes.TEXT.size;

        root = new UIRoot(this, UIContainer.VERTICAL, UIContainer.LEFT);
        root.zeroMargin().zeroPadding().noBackground().noOutline();
        int[] displaySize = app.getWindow().getDisplaySize();
        root.setFixedSize(new SVector(displaySize[0], displaySize[1]));

        init();
    }

    protected abstract void init();

    public void setRootSize(int width, int height) {
        root.setFixedSize(new SVector(width, height));
    }

    public static <T extends UIElement> T setButtonStyle1(T element) {
        element.setOutlineNormal(true);
        element.setBackgroundHighlight(true);
        element.setDefaultStyle();
        return element;
    }

    public static <T extends UIElement> T setButtonStyle2(T element, Supplier<Boolean> selectedSupplier) {
        Supplier<SVector> backgroundColorSupplier = () -> selectedSupplier.get() ? Colors.getBackgroundHighlightColor2()
                : null;
        Supplier<SVector> outlineColorSupplier = () -> element.mouseAbove() ? Colors.getOutlineNormalColor() : null;
        Supplier<Double> strokeWeightSupplier = () -> Sizes.STROKE_WEIGHT.size;
        element.setStyle(new UIStyle(backgroundColorSupplier, outlineColorSupplier, strokeWeightSupplier));
        return element;
    }

    public boolean mouseAboveTextInput() {
        return mouseAboveTextInput(root);
    }

    private boolean mouseAboveTextInput(UIElement element) {
        if (element instanceof UITextInput && element.mouseAbove()) {
            return true;
        }
        if (element instanceof UIContainer container) {
            for (UIElement child : container.getChildren()) {
                if (mouseAboveTextInput(child)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public double textWidth(String text, double textSize, String fontName) {
        TextFont font = app.getLoader().loadFont(fontName);
        return font.textWidth(text) * textSize / font.getSize();
    }

    @Override
    public SVector getDefaultTextColor() {
        return Colors.getTextColor();
    }

    @Override
    public SVector getBackgroundNormalColor() {
        return Colors.getBackgroundNormalColor();
    }

    @Override
    public SVector getBackgroundHighlightColor() {
        return Colors.getBackgroundHighlightColor();
    }

    @Override
    public SVector getOutlineNormalColor() {
        return Colors.getOutlineNormalColor();
    }

    @Override
    public SVector getOutlineHighlightColor() {
        return Colors.getOutlineHighlightColor();
    }

    @Override
    public SVector getSeparatorColor() {
        return Colors.getSeparatorColor();
    }

    @Override
    public double getStrokeWeight() {
        return Sizes.STROKE_WEIGHT.size;
    }
}