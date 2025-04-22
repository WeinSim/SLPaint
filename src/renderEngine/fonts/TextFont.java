package renderEngine.fonts;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import org.lwjglx.util.vector.Matrix3f;

import renderEngine.Loader;
import renderEngine.RawModel;
// import renderEngine.drawcalls.DrawVAO;
// import renderEngine.drawcalls.TextData;
// import renderEngine.drawcalls.TextDrawCall;

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

    // public RawModel createGiantVAO(DrawVAO<TextDrawCall, TextData> textVAO,
    // Loader loader) {

    // int totalTextLength = textVAO.getVertexCount();
    // int[] charIDsArray = new int[totalTextLength];
    // float[] vertices = new float[totalTextLength * 3];
    // int[] dataIndices = new int[totalTextLength];

    // int index = 0;
    // for (TextDrawCall drawCall : textVAO.getDrawCalls()) {
    // String text = drawCall.getText();
    // Matrix3f transformationMatrix = drawCall.getTransformationMatrix();

    // double x = 0;
    // for (FontChar fontChar : toChars(text)) {
    // charIDsArray[index] = charIDs.get((char) fontChar.id());

    // // this could be offloaded to the shader by creating a UBO for transformation
    // // matrices (similar to the UBO for color, size and bounding box)
    // float baseX = (float) x + fontChar.xOffset(),
    // baseY = fontChar.yOffset() - base + 0.8f * size;
    // float vertexX = transformationMatrix.m00 * baseX
    // + transformationMatrix.m10 * baseY
    // + transformationMatrix.m20;
    // float vertexY = transformationMatrix.m01 * baseX
    // + transformationMatrix.m11 * baseY
    // + transformationMatrix.m21;
    // // SVector vertexPos = new SVector(x + fontChar.xOffset(), fontChar.yOffset()
    // -
    // // base + 0.8 * size);
    // // Vector3f vertexPos3f = new Vector3f((float) vertexPos.x, (float)
    // vertexPos.y,
    // // 1.0f);
    // // Matrix3f.transform(transformationMatrix, vertexPos3f, vertexPos3f);
    // // vertexPos.set(vertexPos3f.x, vertexPos3f.y, drawCall.depth());

    // vertices[3 * index] = vertexX;
    // vertices[3 * index + 1] = vertexY;
    // vertices[3 * index + 2] = (float) drawCall.getDepth();

    // dataIndices[index] = drawCall.getDataIndex();

    // x += fontChar.xAdvance();

    // index++;
    // }
    // }

    // return loader.loadToVAO(charIDsArray,vertices,dataIndices);

    // }

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