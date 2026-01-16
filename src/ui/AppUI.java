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

    private double uiScale;

    public AppUI(T app) {
        this.app = app;

        float[] scale = app.getWindow().getWindowContentScale();
        uiScale = Math.sqrt(scale[0] * scale[1]);

        margin = getSize(Sizes.MARGIN);
        padding = getSize(Sizes.PADDING);

        defaultTextSize = getSize(Sizes.TEXT);

        root = new UIRoot(this, UIContainer.VERTICAL, UIContainer.LEFT);
        root.zeroMargin().zeroPadding().noOutline().withBackground();
        int[] displaySize = app.getWindow().getDisplaySize();
        root.setFixedSize(new SVector(displaySize[0], displaySize[1]));

        init();
    }

    protected abstract void init();

    public void setRootSize(int width, int height) {
        root.setFixedSize(new SVector(width, height));
    }

    public static <E extends UIElement> E setSelectableButtonStyle(E element, Supplier<Boolean> selectedSupplier) {
        Supplier<Vector4f> backgroundColorSupplier = () -> selectedSupplier.get()
                ? Colors.backgroundHighlight2()
                : null;
        Supplier<Vector4f> outlineColorSupplier = () -> element.mouseAbove() ? Colors.outlineNormal() : null;
        Supplier<Double> strokeWeightSupplier = () -> element.getPanel().strokeWeightSize();
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

    @Override
    public Vector4f defaultTextColor() {
        return Colors.text();
    }

    @Override
    public Vector4f backgroundNormalColor() {
        return Colors.backgroundNormal();
    }

    @Override
    public Vector4f backgroundHighlightColor() {
        return Colors.backgroundHighlight();
    }

    @Override
    public Vector4f strokeNormalColor() {
        return Colors.outlineNormal();
    }

    @Override
    public Vector4f strokeHighlightColor() {
        return Colors.outlineHighlight();
    }

    @Override
    public Vector4f separatorColor() {
        return Colors.separator();
    }

    // public SVector mainAppSize() {
    // return getWidthHeight(Sizes.MAIN_APP);
    // }

    // public SVector settingsAppSize() {
    // return getWidthHeight(Sizes.SETTINGS_APP);
    // }

    @Override
    public double strokeWeightSize() {
        return getSize(Sizes.STROKE_WEIGHT);
    }

    @Override
    public double defaultTextSize() {
        return getSize(Sizes.TEXT);
    }

    @Override
    public double smallTextSize() {
        return getSize(Sizes.TEXT_SMALL);
    }

    // public double marginSize() {
    // return getSize(Sizes.MARGIN);
    // }

    // public double paddingSize() {
    // return getSize(Sizes.PADDING);
    // }

    // public double scaleSize() {
    // return getSize(Sizes.SCALE);
    // }

    // public double colorButtonSize() {
    // return getSize(Sizes.COLOR_BUTTON);
    // }

    // public double bigColorButtonSize() {
    // return getSize(Sizes.BIG_COLOR_BUTTON);
    // }

    // public SVector colorPickerPreviewSize() {
    // return getWidthHeight(Sizes.COLOR_PICKER_PREVIEW);
    // }

    // public double checkerboardSize() {
    // return getSize(Sizes.CHECKERBOARD);
    // }

    // public double colorPickerPanelSize() {
    // return getSize(Sizes.COLOR_PICKER_PANEL);
    // }

    // public double colorPickerExtraWindowSize() {
    // return getSize(Sizes.COLOR_PICKER_EXTRA_WINDOW);
    // }

    // public double sizeKnobSize() {
    // return getSize(Sizes.SIZE_KNOB);
    // }

    public double getSize(Sizes s) {
        return getSize(s.size, s.forceInteger);
    }

    private double getSize(double s, boolean forceInteger) {
        double size = s * uiScale;
        if (forceInteger) {
            size = (int) Math.round(size);
        }
        return size;
    }

    public SVector getWidthHeight(Sizes s) {
        return new SVector(getSize(s.width, s.forceInteger), getSize(s.height, s.forceInteger));
    }

    public double getUIScale() {
        return uiScale;
    }
}