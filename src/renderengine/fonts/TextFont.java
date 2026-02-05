package renderengine.fonts;

import java.util.ArrayList;
import java.util.HashMap;

import renderengine.bufferobjects.FloatVBO;
import renderengine.bufferobjects.IntVBO;
import renderengine.drawcalls.TextDrawCall;
import sutil.math.SVector;

public class TextFont {

    private static final char UNKNOWN_CHAR = 9633;

    private HashMap<Character, Integer> charIDs;
    private FontChar[] fontChars;
    private float[] uboData;

    private int unknownCharIndex;

    private String name;
    private int size;
    private int lineHeight;
    private int base;

    private int[] textureIDs;
    private int textureWidth, textureHeight;

    public TextFont(String name, int size, int lineHeight, int base, int[] textureIDs, int textureWidth,
            int textureHeight) {

        this.name = name;
        this.size = size;
        this.lineHeight = lineHeight;
        this.base = base;
        this.textureIDs = textureIDs;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
    }

    public void loadChars(ArrayList<FontChar> chars) {
        fontChars = new FontChar[chars.size()];
        charIDs = new HashMap<>();
        unknownCharIndex = -1;
        int i = 0;
        for (FontChar fontChar : chars) {
            char c = (char) fontChar.id();
            if (c == UNKNOWN_CHAR) {
                unknownCharIndex = i;
            }
            charIDs.put(c, i);
            fontChars[i++] = fontChar;
        }

        if (unknownCharIndex == -1) {
            final String baseString = "\"Unknown character\" (\"\u9633\", id %d) missing from font!\n";
            throw new RuntimeException(String.format(baseString, UNKNOWN_CHAR));
        }
    }

    private FontChar[] toChars(String text) {
        return toChars(text, text.length());
    }

    private FontChar[] toChars(String text, int len) {
        FontChar[] chars = new FontChar[len];
        char[] charArray = text.toCharArray();
        for (int i = 0; i < len; i++) {
            chars[i] = fontChars[charIDs.getOrDefault(charArray[i], unknownCharIndex)];
        }
        return chars;
    }

    public void putCharsIntoVBOs(TextDrawCall drawCall, IntVBO dataIndex, FloatVBO position, FloatVBO depth,
            IntVBO charIndex, int batchIndex) {

        double x = drawCall.position.x,
                y = drawCall.position.y;
        for (FontChar fontChar : toChars(drawCall.text)) {
            dataIndex.putData(batchIndex);

            charIndex.putData(charIDs.get((char) fontChar.id()));

            SVector vertexPos = new SVector(x + fontChar.xOffset() * drawCall.relativeSize,
                    y + (fontChar.yOffset() - base + 0.8 * size) * drawCall.relativeSize);
            position.putData(vertexPos);

            depth.putData(drawCall.depth);

            x += fontChar.xAdvance() * drawCall.relativeSize;
        }
    }

    public double textWidth(String text) {
        return textWidth(text, text.length());
    }

    public double textWidth(String text, int len) {
        FontChar[] chars = toChars(text, len);
        double sum = 0;
        for (FontChar fontChar : chars) {
            sum += fontChar.xAdvance();
        }
        return sum;
    }

    public int getCharIndex(String text, double x) {
        FontChar[] chars = toChars(text);

        if (chars.length == 0)
            return 0;

        double sum = 0;
        int index = chars.length;
        for (int i = 0; i < chars.length; i++) {
            double current = sum,
                    next = sum + chars[i].xAdvance();
            double middle = (current + next) / 2;
            if (middle > x) {
                index = i;
                break;
            }

            sum = next;
        }

        return index;
    }

    public int[] getTextureIDs() {
        return textureIDs;
    }

    public String getName() {
        return name;
    }

    public int getSize() {
        return size;
    }

    public int getLineHeight() {
        return lineHeight;
    }

    public int getTextureWidth() {
        return textureWidth;
    }

    public int getTextureHeight() {
        return textureHeight;
    }

    public FontChar[] getFontChars() {
        return fontChars;
    }

    public float[] getUBOData() {
        return uboData;
    }

    public void setUBOData(float[] uboData) {
        this.uboData = uboData;
    }
}