package ui;

import main.apps.App;
import renderEngine.fonts.TextFont;
import sutil.math.SVector;
import sutil.ui.UIContainer;
import sutil.ui.UIElement;
import sutil.ui.UIGetter;
import sutil.ui.UIPanel;
import sutil.ui.UIRoot;
import sutil.ui.UIStyle;
import sutil.ui.UITextInput;

public abstract class AppUI<T extends App> extends UIPanel{

    protected T app;

    private TextFont courierNew;

    public AppUI(T app) {
        this.app = app;

        textSize = 18;
        courierNew = app.getLoader().loadFont("Courier New Bold", (int) textSize, false);

        root = new UIRoot(this, UIContainer.HORIZONTAL, UIContainer.TOP);
        root.zeroMargin().zeroPadding().noBackground().noOutline();
        int[] displaySize = app.getWindow().getDisplaySize();
        root.setFixedSize(new SVector(displaySize[0], displaySize[1]));

        init();

        updateSize();
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

    public static <T extends UIElement> T setButtonStyle2(T element, UIGetter<Boolean> selectedGetter) {
        UIGetter<SVector> backgroundColorGetter = () -> selectedGetter.get() ? App.getBackgroundHighlightColor2()
                : null;
        UIGetter<SVector> outlineColorGetter = () -> element.mouseAbove() ? App.getOutlineHighlightColor() : null;
        UIGetter<Double> strokeWeightGetter = () -> 1.0;
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
        return App.getBackgroundNormalColor();
    }

    @Override
    public SVector getBackgroundHighlightColor() {
        return App.getBackgroundHighlightColor();
    }

    @Override
    public SVector getOutlineNormalColor() {
        return App.getOutlineNormalColor();
    }

    @Override
    public SVector getOutlineHighlightColor() {
        return App.getOutlineHighlightColor();
    }
}