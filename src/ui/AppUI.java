package ui;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.function.Supplier;

import org.lwjglx.util.vector.Vector4f;

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
        root.zeroMargin().zeroPadding().noOutline();
        int[] displaySize = app.getWindow().getDisplaySize();
        root.setFixedSize(new SVector(displaySize[0], displaySize[1]));

        init();
    }

    protected abstract void init();

    public void setRootSize(int width, int height) {
        root.setFixedSize(new SVector(width, height));
    }

    public static <T extends UIElement> T setSelectableButtonStyle(T element, Supplier<Boolean> selectedSupplier) {
        Supplier<Vector4f> backgroundColorSupplier = () -> selectedSupplier.get()
                ? Colors.getBackgroundHighlightColor2()
                : null;
        Supplier<Vector4f> outlineColorSupplier = () -> element.mouseAbove() ? Colors.getOutlineNormalColor() : null;
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
    public double textWidth(String text, double textSize, String fontName, int len) {
        TextFont font = app.getLoader().loadFont(fontName);
        return font.textWidth(text, len) * textSize / font.getSize();
    }

    @Override
    public int getCharIndex(String text, double textSize, String fontName, double x) {
        TextFont font = app.getLoader().loadFont(fontName);
        return font.getCharIndex(text, x / textSize * font.getSize());
    }

    @Override
    public Vector4f getDefaultTextColor() {
        return Colors.getTextColor();
    }

    @Override
    public Vector4f getBackgroundNormalColor() {
        return Colors.getBackgroundNormalColor();
    }

    @Override
    public Vector4f getBackgroundHighlightColor() {
        return Colors.getBackgroundHighlightColor();
    }

    @Override
    public Vector4f getStrokeNormalColor() {
        return Colors.getOutlineNormalColor();
    }

    @Override
    public Vector4f getStrokeHighlightColor() {
        return Colors.getOutlineHighlightColor();
    }

    @Override
    public Vector4f getSeparatorColor() {
        return Colors.getSeparatorColor();
    }

    @Override
    public double getStrokeWeight() {
        return Sizes.STROKE_WEIGHT.size;
    }

    /**
     * Returns a {@code String[]} containing the first {@code numWords} words of
     * lorem ipsum, split into lines of {@code lineLength} words each.
     * 
     * @param numWords
     * @param lineLength
     * @return
     */
    public static String[] lipsum(int numWords, int lineLength) {
        String lipsum = "";
        try (BufferedReader reader = new BufferedReader(new FileReader("res/misc/lipsum.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lipsum += line;
                lipsum += "\n ";
            }
        } catch (IOException e) {
            e.printStackTrace();
            return new String[] { "[unable to load lipsum]" };
        }

        String[] words = lipsum.split(" ");
        numWords = Math.min(numWords, words.length);
        String[] ret = new String[numWords / lineLength];
        int index = 0;
        for (int i = 0; i < ret.length; i++) {
            ret[i] = "";
            for (int j = 0; j < lineLength; j++) {
                String nextWord = words[index++];
                boolean newLine = nextWord.endsWith("\n");
                if (newLine) {
                    ret[i] += nextWord.substring(0, nextWord.length() - 1);
                    break;
                } else {
                    ret[i] += nextWord;
                    ret[i] += " ";
                }
            }
        }

        return ret;
    }
}