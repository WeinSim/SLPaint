package renderEngine.fonts;

import java.util.ArrayList;
import java.util.HashMap;

import renderEngine.Loader;
import renderEngine.ModelTexture;
import renderEngine.RawModel;
import sutil.math.SVector;

public class TextFont {

    private static final char UNKNOWN_CHAR_ID = 9633;

    // private App app;

    private HashMap<Character, FontChar> characters;

    private String name;
    private int size;
    private int lineHeight;
    private int base;

    private ModelTexture texture;

    public TextFont(String name, int size, int lineHeight, int base, ModelTexture texture) {
        // this.app = app;
        this.name = name;
        this.size = size;
        this.lineHeight = lineHeight;
        this.base = base;
        this.texture = texture;
    }

    public void loadChars(ArrayList<FontChar> chars) {
        characters = new HashMap<>();
        for (FontChar fontChar : chars) {
            characters.put((char) fontChar.id, fontChar);
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
        ArrayList<SVector> sizes = new ArrayList<>();
        ArrayList<SVector> textureCoords = new ArrayList<>();

        double x = 0;
        for (FontChar fontChar : toChars(text)) {
            vertices.add(new SVector(x + fontChar.xOffset, fontChar.yOffset - base + 0.8 * size));
            sizes.add(new SVector(fontChar.width, fontChar.height));
            textureCoords.add(new SVector(fontChar.x, fontChar.y));
            x += fontChar.xAdvance;
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

        // return app.getLoader().loadToTextVAO(verticesArray, textureCoordsArray, sizesArray);
        return loader.loadToTextVAO(verticesArray, textureCoordsArray, sizesArray);
    }

    public double textWidth(String text) {
        ArrayList<FontChar> chars = toChars(text);
        double sum = 0;
        for (FontChar fontChar : chars) {
            sum += fontChar.xAdvance;
        }
        return sum;
    }

    public ModelTexture getTexture() {
        return texture;
    }

    public static char getUnknownCharId() {
        return UNKNOWN_CHAR_ID;
    }

    // public Game getGame() {
    //     return game;
    // }

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
}