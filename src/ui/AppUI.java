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

public abstract class AppUI<T extends App> extends UIPanel{

    protected T app;

    private TextFont courierNew;

    public AppUI(T app) {
        this.app = app;

        margin = Sizes.MARGIN.size;
        padding = Sizes.PADDING.size;

        textSize = Sizes.TEXT.size;
        courierNew = app.getLoader().loadFont("Courier New Bold", (int) textSize, false);

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

    public static <T extends UIElement> T setButtonStyle2(T element, Supplier<Boolean> selectedGetter) {
        Supplier<SVector> backgroundColorGetter = () -> selectedGetter.get() ? Colors.getBackgroundHighlightColor2()
                : null;
        Supplier<SVector> outlineColorGetter = () -> element.mouseAbove() ? Colors.getOutlineNormalColor() : null;
        Supplier<Double> strokeWeightGetter = () -> Sizes.STROKE_WEIGHT.size;
        element.setStyle(new UIStyle(backgroundColorGetter, outlineColorGetter, strokeWeightGetter));
        return element;
    }

    public TextFont getFont() {
        return courierNew;
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
    public double textWidth(String text) {
        return courierNew.textWidth(text) * textSize / courierNew.getSize();
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
    public double getStrokeWeight() {
        return Sizes.STROKE_WEIGHT.size;
    }
}