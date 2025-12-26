package renderEngine.fonts;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import renderEngine.shaders.bufferobjects.VBOFloatData;
import renderEngine.shaders.bufferobjects.VBOIntData;
import renderEngine.shaders.drawcalls.TextDrawCall;
import sutil.math.SVector;

public class TextFont {

    private static final char UNKNOWN_CHAR = 9633;

    private HashMap<Character, Integer> charIDs;
    private FontChar[] fontChars;
    private FloatBuffer uboData;

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
        FontChar[] chars = new FontChar[text.length()];
        int i = 0;
        for (Character c : text.toCharArray()) {
            chars[i++] = fontChars[charIDs.getOrDefault(c, unknownCharIndex)];
        }
        return chars;
    }

    public void putCharsIntoVBOs(TextDrawCall drawCall, VBOIntData dataIndex, VBOFloatData position, VBOFloatData depth,
            VBOIntData charIndex, int batchIndex) {

        double x = drawCall.position.x,
                y = drawCall.position.y;
        for (FontChar fontChar : toChars(drawCall.text)) {
            dataIndex.putData(batchIndex);

            charIndex.putData(charIDs.get((char) fontChar.id()));

            SVector vertexPos = new SVector(x + fontChar.xOffset() * drawCall.relativeSize,
                    y + (fontChar.yOffset() -base + 0.8 * size) * drawCall.relativeSize);
            position.putData(vertexPos);

            depth.putData(drawCall.depth);

            x += fontChar.xAdvance() * drawCall.relativeSize;
        }
    }

    public double textWidth(String text) {
        FontChar[] chars = toChars(text);
        double sum = 0;
        for (FontChar fontChar : chars) {
            sum += fontChar.xAdvance();
        }
        return sum;
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

    public FloatBuffer getUBOData() {
        return uboData;
    }

    public void setUBOData(FloatBuffer uboData) {
        this.uboData = uboData;
    }
}