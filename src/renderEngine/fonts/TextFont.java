package renderEngine.fonts;

import java.util.ArrayList;
import java.util.HashMap;

import renderEngine.Loader;
import renderEngine.RawModel;
import sutil.math.SVector;

public class TextFont {

    private static final char UNKNOWN_CHAR_ID = 9633;

    private HashMap<Character, FontChar> characters;

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
        characters = new HashMap<>();
        for (FontChar fontChar : chars) {
            characters.put((char) fontChar.id(), fontChar);
        }
    }

    private ArrayList<FontChar> toChars(String text) {
        ArrayList<FontChar> chars = new ArrayList<>();
        for (Character c : text.toCharArray()) {
            FontChar fontChar = characters.get(c);
            if (fontChar == null) {
                fontChar = characters.get(UNKNOWN_CHAR_ID);
            }
            if (fontChar == null) {
                System.out.format("\"Unknown character\" (\"\u9633\", id %d) missing from font!\n", UNKNOWN_CHAR_ID);
                System.exit(1);
            }
            chars.add(fontChar);
        }
        return chars;
    }

    public RawModel generateVAO(String text, Loader loader) {
        ArrayList<SVector> vertices = new ArrayList<>();
        ArrayList<SVector> textureCoords = new ArrayList<>();
        ArrayList<Integer> pages = new ArrayList<>();
        ArrayList<SVector> sizes = new ArrayList<>();

        double x = 0;
        for (FontChar fontChar : toChars(text)) {
            vertices.add(new SVector(x + fontChar.xOffset(), fontChar.yOffset() - base + 0.8 * size));
            textureCoords.add(new SVector(fontChar.x(), fontChar.y()));
            pages.add(fontChar.page());
            sizes.add(new SVector(fontChar.width(), fontChar.height()));

            x += fontChar.xAdvance();
        }

        double[] verticesArray = new double[vertices.size() * 2];
        double[] textureCoordsArray = new double[vertices.size() * 2];
        int[] pagesArray = new int[pages.size()];
        double[] sizesArray = new double[vertices.size() * 2];
        for (int i = 0; i < vertices.size(); i++) {
            SVector vertex = vertices.get(i);
            verticesArray[2 * i] = vertex.x;
            verticesArray[2 * i + 1] = vertex.y;

            SVector textureCoord = textureCoords.get(i);
            textureCoordsArray[2 * i] = textureCoord.x;
            textureCoordsArray[2 * i + 1] = textureCoord.y;

            pagesArray[i] = pages.get(i);

            SVector size = sizes.get(i);
            sizesArray[2 * i] = size.x;
            sizesArray[2 * i + 1] = size.y;
        }

        // return app.getLoader().loadToTextVAO(verticesArray, textureCoordsArray,
        // sizesArray);
        return loader.loadToTextVAO(verticesArray, textureCoordsArray, pagesArray, sizesArray);
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

    public HashMap<Character, FontChar> getCharacters() {
        return characters;
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
}