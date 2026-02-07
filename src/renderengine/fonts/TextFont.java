package renderengine.fonts;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

import main.apps.MainApp;
import renderengine.UIRenderMaster;
import renderengine.bufferobjects.FloatVBO;
import renderengine.bufferobjects.IntVBO;
import renderengine.drawcalls.TextDrawCall;
import sutil.SUtil;
import sutil.math.SVector;
import sutil.ui.UI;

public class TextFont {

    private static final String FONT_DIRECTORY = "res/fonts/";

    public static final String DEFAULT_FONT_NAME = "FreeMonoBold";

    private static final char UNKNOWN_CHAR = 0x25A1; // □ (WHITE SQUARE)

    private static HashMap<String, TextFont> fontCache = new HashMap<>();

    public final String name;
    public final int size;
    public final int lineHeight;
    public final int base;

    private final String[] textureFilenames;
    public final int textureWidth, textureHeight;

    private final FontChar[] fontChars;
    private final HashMap<Character, Integer> charIDs;
    private final int unknownCharIndex;
    private final float[] uboData;

    public TextFont(String name, int size, int lineHeight, int base, String[] textureFilenames,
            int textureWidth, int textureHeight, FontChar[] fontChars, HashMap<Character, Integer> charIDs,
            int unknownCharIndex, float[] uboData) {

        this.name = name;
        this.size = size;
        this.lineHeight = lineHeight;
        this.base = base;
        this.textureFilenames = textureFilenames;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.fontChars = fontChars;
        this.charIDs = charIDs;
        this.unknownCharIndex = unknownCharIndex;
        this.uboData = uboData;
    }

    public static TextFont load(String name) throws IOException {
        // this is just a hack for now
        int fontSize = UI.getUIScale() > 1.5 ? 36 : 18;
        fontSize = 36;

        String directoryName = getDirectoryName(name);

        String fontInfoFile = String.format("%s/output_%d.fnt", directoryName, fontSize);
        List<String> allLines = null;
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(fontInfoFile))) {
            allLines = bufferedReader.readAllLines();
        } catch (IOException e) {
            loadFail("Unable to read file %s", name, fontInfoFile);
        }

        ArrayList<FontChar> chars = new ArrayList<>();
        int size = 0, lineHeight = 0, base = 0, pages = 0, textureWidth = 0, textureHeight = 0;
        String[] textureFilenames = null;
        for (String line : allLines) {
            ArrayList<String> parts = new ArrayList<>();
            for (String string : line.split(" ")) {
                if (!string.isEmpty())
                    parts.add(string);
            }
            if (parts.isEmpty())
                continue;
            String lineType = parts.get(0);
            HashMap<String, Object> properties = new HashMap<>();
            for (int i = 1; i < parts.size(); i++) {
                String[] keyValue = parts.get(i).split("=");
                if (keyValue.length != 2)
                    continue;
                String key = keyValue[0];
                if (keyValue[1].startsWith("\"")) {
                    properties.put(key, keyValue[1].replaceAll("\"", ""));
                } else {
                    try {
                        properties.put(key, Integer.parseInt(keyValue[1]));
                    } catch (NumberFormatException e) {
                    }
                }
            }
            switch (lineType) {
                case "info" -> {
                    size = Math.abs((int) properties.get("size"));
                }
                case "common" -> {
                    lineHeight = (int) properties.get("lineHeight");
                    base = (int) properties.get("base");
                    pages = (int) properties.get("pages");
                    textureFilenames = new String[pages];
                    textureWidth = (int) properties.get("scaleW");
                    textureHeight = (int) properties.get("scaleH");
                }
                case "page" -> {
                    int id = (int) properties.get("id");
                    textureFilenames[id] = (String) properties.get("file");
                }
                case "char" ->
                    chars.add(new FontChar(
                            (int) properties.get("id"),
                            (pages > 1) ? (int) properties.get("page") : 0,
                            (int) properties.get("x"),
                            (int) properties.get("y"),
                            (int) properties.get("width"),
                            (int) properties.get("height"),
                            (int) properties.get("xoffset"),
                            (int) properties.get("yoffset"),
                            (int) properties.get("xadvance")));
            }
        }

        if (chars.size() > UIRenderMaster.MAX_FONT_CHARS)
            loadFail("Too many characters (%d). Maximum is %d.", name, chars.size(), UIRenderMaster.MAX_FONT_CHARS);

        if (pages > UIRenderMaster.MAX_FONT_ATLASSES)
            loadFail("Too many texture atlasses (%d). Maximum is %d.", name, pages, UIRenderMaster.MAX_FONT_ATLASSES);

        FontChar[] fontChars = new FontChar[chars.size()];
        HashMap<Character, Integer> charIDs = new HashMap<>();
        float[] uboData = new float[UIRenderMaster.MAX_FONT_CHARS * 4 + 2];
        int unknownCharIndex = -1;
        int i = 0;
        int uboIndex = 0;
        for (FontChar fontChar : chars) {
            char c = (char) fontChar.id();
            if (c == UNKNOWN_CHAR)
                unknownCharIndex = i;
            charIDs.put(c, i);
            uboData[uboIndex++] = fontChar.x() + textureWidth * fontChar.page();
            uboData[uboIndex++] = fontChar.y();
            uboData[uboIndex++] = fontChar.width();
            uboData[uboIndex++] = fontChar.height();

            fontChars[i++] = fontChar;
        }
        uboIndex = UIRenderMaster.MAX_FONT_CHARS * 4;
        uboData[uboIndex++] = textureWidth;
        uboData[uboIndex++] = textureHeight;

        if (unknownCharIndex == -1)
            loadFail("\"Unknown character\" (\"\u9633\", id %d) missing", name, UNKNOWN_CHAR);

        return new TextFont(name, size, lineHeight, base, textureFilenames, textureWidth, textureHeight, fontChars,
                charIDs, unknownCharIndex, uboData);
    }

    private static String getDirectoryName(String name) {
        return String.format("%s%s/", FONT_DIRECTORY, name);
    }

    private static void loadFail(String message, String name, Object... params) throws IOException {
        message = String.format(message, params);
        message = String.format("Could not load font \"%s\": %s", name, message);
        throw new IOException(message);
    }

    public static void createFontAtlas(String name, int textSize) {
        // doesn't work, don't know why
        // delete old files
        // ArrayList<String> deleteArgs = new ArrayList<>();
        // deleteArgs.add("rm");
        // deleteArgs.add("output*");
        // MainApp.runCommand(String.format("%s%s", FONT_DIRECTORY, name), deleteArgs);

        String directoryName = getDirectoryName(name);
        MainApp.runCommand(directoryName,
                getFontGenerationCommand(name, 2, 256, 256, textSize, new int[] { 32, 126, 160, 255 },
                        new int[] { 9633 }, 0));
    }

    private static void addArgument(ArrayList<String> commands, String argument, int value) {
        addArgument(commands, argument, Integer.toString(value));
    }

    private static void addArgument(ArrayList<String> commands, String argument, String value) {
        commands.add("--" + argument);
        commands.add(value);
    }

    private static ArrayList<String> getFontGenerationCommand(String fontName, int padding, int textureWidth,
            int textureHeight, int fontSize, int[] charRanges, int[] extraChars, int bgColor) {

        ArrayList<String> commands = new ArrayList<>();
        // commands.add("/home/simon/code/executables/fontbm/fontbm");
        commands.add("fontbm");
        addArgument(commands, "font-file", "%s.ttf".formatted(fontName));
        addArgument(commands, "output", "output");
        addArgument(commands, "padding-up", padding);
        addArgument(commands, "padding-down", padding);
        addArgument(commands, "padding-left", padding);
        addArgument(commands, "padding-right", padding);
        addArgument(commands, "texture-size", "%dx%d".formatted(textureWidth, textureHeight));
        addArgument(commands, "font-size", fontSize);
        StringBuilder charsBuilder = new StringBuilder();
        for (int i = 0; i < charRanges.length / 2; i++) {
            charsBuilder.append("%d-%d,".formatted(charRanges[2 * i], charRanges[2 * i + 1]));
        }
        for (int extraChar : extraChars) {
            charsBuilder.append("%d,".formatted(extraChar));
        }
        int len = charsBuilder.length();
        if (len > 0) {
            charsBuilder.deleteCharAt(len - 1);
        }
        addArgument(commands, "chars", charsBuilder.toString());
        addArgument(commands, "background-color", "%d,%d,%d".formatted(
                SUtil.red(bgColor), SUtil.green(bgColor), SUtil.blue(bgColor)));

        // System.out.print("Generated command: ");
        // for (String str : commands) {
        // System.out.print(str + " ");
        // }
        // System.out.println();

        return commands;
    }

    public static TextFont getFont(String name) {
        // try returning already loaded font
        TextFont font = fontCache.get(name);
        if (font != null)
            return font;

        // load new font
        try {
            font = TextFont.load(name);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        fontCache.put(name, font);
        return font;
    }

    public static TextFont getDefaultFont() {
        return getFont(DEFAULT_FONT_NAME);
    }

    public int[] loadTextures() throws IOException {
        String directoryName = getDirectoryName(name);
        int[] textureIDs = new int[textureFilenames.length];
        for (int i = 0; i < textureFilenames.length; i++) {
            String textureFile = String.format("%s/%s", directoryName, textureFilenames[i]);
            Texture texture = null;
            try {
                texture = TextureLoader.getTexture("PNG", new FileInputStream(textureFile));
            } catch (Exception e) {
                e.printStackTrace();
                loadFail("Unable to load texture \"%s\"", name, textureFile);
            }
            textureIDs[i] = texture.getTextureID();
        }
        return textureIDs;
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

    public float[] getUBOData() {
        return uboData;
    }
}