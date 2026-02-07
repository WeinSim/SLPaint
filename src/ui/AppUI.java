package ui;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

import org.lwjglx.util.vector.Vector4f;

import main.ColorPicker;
import main.apps.App;
import main.settings.BooleanSetting;
import main.settings.ColorSetting;
import renderengine.fonts.TextFont;
import sutil.SUtil;
import sutil.ui.UI;
import sutil.ui.UIColors;
import sutil.ui.UIElement;
import sutil.ui.UISizes;
import sutil.ui.UIStyle;

public abstract class AppUI<T extends App> extends UI {

    private static final Vector4f[] DEFAULT_UI_COLORS_DARK = {
            new Vector4f(0.3f, 0.3f, 0.3f, 1.0f),
            new Vector4f(0.07f, 0.35f, 0.5f, 1.0f),
            new Vector4f(0.5f, 0.07f, 0.35f, 1.0f),
            new Vector4f(0.42f, 0.14f, 0.14f, 1.0f)
    };

    private static final Vector4f[] DEFAULT_UI_COLORS_LIGHT = {
            new Vector4f(1, 1, 1, 1),
            new Vector4f(0.67f, 0.85f, 0.95f, 1.0f),
            new Vector4f(0.95f, 0.63f, 0.84f, 1.0f),
            new Vector4f(0.84f, 0.51f, 0.51f, 1.0f)
    };

    protected T app;

    private static BooleanSetting darkMode = new BooleanSetting("darkMode");
    private static ColorSetting baseColor = new ColorSetting("baseColor");

    public AppUI(T app) {
        this.app = app;
        float[] scale = app.getWindow().getWindowContentScale();
        super(Math.sqrt(scale[0] * scale[1]), app.getWindowSize());
    }

    public static <E extends UIElement> E setSelectableButtonStyle(E element, BooleanSupplier selectedSupplier) {
        Supplier<Vector4f> backgroundColorSupplier = () -> selectedSupplier.getAsBoolean()
                ? UIColors.BACKGROUND_2.get()
                : null;
        Supplier<Vector4f> outlineColorSupplier = () -> element.mouseAbove()
                ? UIColors.OUTLINE.get()
                : null;
        DoubleSupplier strokeWeightSupplier = UISizes.STROKE_WEIGHT;
        element.setStyle(new UIStyle(backgroundColorSupplier, outlineColorSupplier, strokeWeightSupplier));
        return element;
    }

    @Override
    public double textWidthImpl(String text, double textSize, String fontName, int len) {
        TextFont font = TextFont.getFont(fontName);
        return font.textWidth(text, len) * textSize / font.size;
    }

    @Override
    public int getCharIndexImpl(String text, double textSize, String fontName, double x) {
        TextFont font = TextFont.getFont(fontName);
        return font.getCharIndex(text, x / textSize * font.size);
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

    public static boolean isDarkMode() {
        return darkMode.get();
    }

    @Override
    protected boolean isDarkModeImpl() {
        return isDarkMode();
    }

    public static void setDarkMode(boolean darkMode) {
        AppUI.darkMode.set(darkMode);
    }

    public static Vector4f getBaseColor() {
        int rgb = baseColor.get().getRGB();

        int red = SUtil.red(rgb);
        int green = SUtil.green(rgb);
        int blue = SUtil.blue(rgb);
        int alpha = SUtil.alpha(rgb);
        return (Vector4f) new Vector4f(red, green, blue, alpha).scale(1.0f / 255);
    }

    @Override
    protected Vector4f getBaseColorImpl() {
        return getBaseColor();
    }

    public static Vector4f[] getDefaultUIColors() {
        return isDarkMode() ? DEFAULT_UI_COLORS_DARK : DEFAULT_UI_COLORS_LIGHT;
    }

    public static int getNumDefaultUIColors() {
        return DEFAULT_UI_COLORS_DARK.length;
    }

    public static ColorPicker getBaseColorPicker() {
        return baseColor.get();
    }
}