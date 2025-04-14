package renderEngine.fonts;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import org.lwjglx.util.vector.Matrix3f;
import org.lwjglx.util.vector.Vector3f;

import renderEngine.Loader;
import renderEngine.RawModel;
import renderEngine.UIRenderMaster;
import sutil.math.SVector;

public class TextFont {

    private static final char UNKNOWN_CHAR_ID = 9633;

    private HashMap<Character, Integer> charIDs;
    private FontChar[] fontChars;
    private FloatBuffer uboData;

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
        int i = 0;
        for (FontChar fontChar : chars) {
            charIDs.put((char) fontChar.id(), i);
            fontChars[i++] = fontChar;
        }
    }

    private ArrayList<FontChar> toChars(String text) {
        ArrayList<FontChar> chars = new ArrayList<>();
        for (Character c : text.toCharArray()) {
            Integer index = charIDs.get(c);
            if (index == null) {
                index = charIDs.get(UNKNOWN_CHAR_ID);
            }
            if (index == null) {
                final String baseString = "\"Unknown character\" (\"\u9633\", id %d) missing from font!\n";
                throw new RuntimeException(String.format(baseString, UNKNOWN_CHAR_ID));
            }
            chars.add(fontChars[index]);
        }
        return chars;
    }

    public RawModel generateVAO(String text, Loader loader) {
        ArrayList<SVector> vertices = new ArrayList<>();
        ArrayList<SVector> textureCoords = new ArrayList<>();
        ArrayList<SVector> sizes = new ArrayList<>();

        double x = 0;
        for (FontChar fontChar : toChars(text)) {
            vertices.add(new SVector(x + fontChar.xOffset(), fontChar.yOffset() - base + 0.8 * size));
            textureCoords.add(new SVector(fontChar.x() + textureWidth * fontChar.page(), fontChar.y()));
            sizes.add(new SVector(fontChar.width(), fontChar.height()));

            x += fontChar.xAdvance();
        }

        double[] verticesArray = new double[vertices.size() * 2];
        double[] textureCoordsArray = new double[vertices.size() * 2];
        double[] sizesArray = new double[vertices.size() * 2];
        for (int i = 0; i < vertices.size(); i++) {
            SVector vertex = vertices.get(i);
            verticesArray[2 * i] = vertex.x;
            verticesArray[2 * i + 1] = vertex.y;

            SVector textureCoord = textureCoords.get(i);
            textureCoordsArray[2 * i] = textureCoord.x;
            textureCoordsArray[2 * i + 1] = textureCoord.y;

            SVector size = sizes.get(i);
            sizesArray[2 * i] = size.x;
            sizesArray[2 * i + 1] = size.y;
        }

        // return app.getLoader().loadToTextVAO(verticesArray, textureCoordsArray,
        // sizesArray);
        return loader.generateTextVAO(verticesArray, textureCoordsArray, sizesArray);
    }

    public RawModel createGiantVAO(UIRenderMaster.TextDrawCallList drawCalls, Loader loader) {
        int totalTextLength = drawCalls.totalLength;
        int[] charIDsArray = new int[totalTextLength];
        double[] verticesArray = new double[totalTextLength * 3];
        double[] textSizesArray = new double[totalTextLength];
        double[] colorsArray = new double[totalTextLength * 3];

        int index = 0;
        for (UIRenderMaster.TextDrawCall drawCall : drawCalls.drawCalls) {
            String text = drawCall.text();
            Matrix3f transformationMatrix = drawCall.transformationMatrix();
            SVector color = drawCall.color();

            double scaleFactor = transformationMatrix.m00;

            double x = 0;
            for (FontChar fontChar : toChars(text)) {
                charIDsArray[index] = charIDs.get((char) fontChar.id());

                SVector vertexPos = new SVector(x + fontChar.xOffset(), fontChar.yOffset() - base + 0.8 * size);
                Vector3f vertexPos3f = new Vector3f((float) vertexPos.x, (float) vertexPos.y, 1.0f);
                Matrix3f.transform(transformationMatrix, vertexPos3f, vertexPos3f);
                vertexPos.set(vertexPos3f.x, vertexPos3f.y, drawCall.depth());

                verticesArray[3 * index] = vertexPos.x;
                verticesArray[3 * index + 1] = vertexPos.y;
                verticesArray[3 * index + 2] = vertexPos.z;

                textSizesArray[index] = scaleFactor;

                colorsArray[3 * index] = color.x;
                colorsArray[3 * index + 1] = color.y;
                colorsArray[3 * index + 2] = color.z;

                x += fontChar.xAdvance();

                index++;
            }
        }

        return loader.generateTextVAO(charIDsArray, verticesArray, textSizesArray, colorsArray);
    }

    public double textWidth(String text) {
        ArrayList<FontChar> chars = toChars(text);
        double sum = 0;
        for (FontChar fontChar : chars) {
            sum += fontChar.xAdvance();
        }
        return sum;
    }

    public int[] getTextureIDs() {
        return textureIDs;
    }

    public static char getUnknownCharId() {
        return UNKNOWN_CHAR_ID;
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