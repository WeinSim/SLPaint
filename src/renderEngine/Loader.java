package renderEngine;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

import main.apps.MainApp;
import renderEngine.fonts.FontChar;
import renderEngine.fonts.TextFont;
import renderEngine.shaders.bufferobjects.FrameBufferObject;
import renderEngine.shaders.bufferobjects.UniformBufferObject;
import renderEngine.shaders.bufferobjects.VBOData;
import sutil.SUtil;

public class Loader {

    public static final String FONT_DIRECTORY = "res/fonts/";

    private ArrayList<Integer> vaos;
    private ArrayList<Integer> vbos;

    private ArrayList<Integer> textures;
    private ArrayList<Integer> fbos;

    private HashMap<String, TextFont> loadedFonts;

    public Loader() {
        vaos = new ArrayList<>();
        vbos = new ArrayList<>();
        textures = new ArrayList<>();
        fbos = new ArrayList<>();

        loadedFonts = new HashMap<>();
    }

    private int createVAO() {
        int vaoID = GL30.glGenVertexArrays();
        vaos.add(vaoID);
        GL30.glBindVertexArray(vaoID);
        return vaoID;
    }

    public RawModel loadToVAO(VBOData[] vboData, int vertexCount) {
        int vaoID = createVAO();
        int attributeNumber = 0;
        for (VBOData vbo : vboData) {
            vbos.add(vbo.storeDataInAttributeList(attributeNumber));
            attributeNumber += vbo.getNumAttributes();
        }
        GL30.glBindVertexArray(0);
        return new RawModel(vaoID, vertexCount, attributeNumber);
    }

    public void loadToUBO(UniformBufferObject ubo, FloatBuffer data) {
        data.flip();
        int bufferID = ubo.getBufferID();
        if (bufferID != 0) {
            GL15.glDeleteBuffers(bufferID);
        }
        bufferID = GL15.glGenBuffers();
        ubo.setBufferID(bufferID);

        GL15.glBindBuffer(GL31.GL_UNIFORM_BUFFER, bufferID);
        GL15.glBufferData(GL31.GL_UNIFORM_BUFFER, data, GL15.GL_STATIC_DRAW);
        GL15.glBindBuffer(GL31.GL_UNIFORM_BUFFER, 0);

        GL31.glBindBufferBase(GL31.GL_UNIFORM_BUFFER, ubo.getBinding(), bufferID);
    }

    // https://learnopengl.com/Advanced-OpenGL/Framebuffers
    public FrameBufferObject createFBO(int width, int height) {
        // create framebuffer
        int fbo = GL30.glGenFramebuffers();
        fbos.add(fbo);
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo);

        // create texture
        int texture = GL11.glGenTextures();
        textures.add(texture);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE,
                (int[]) null);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL30.GL_LINEAR_MIPMAP_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        // unbind texture
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        // attach texture to framebuffer
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, texture, 0);

        return new FrameBufferObject(fbo, texture, width, height);
    }

    public int loadTexture(String path) {
        Texture texture = null;
        try {
            texture = TextureLoader.getTexture("PNG", new FileInputStream(path));
        } catch (Exception e) {
            System.err.format("Could not load texture \"%s\"!\n", path);
            e.printStackTrace();
            System.exit(-1);
        }
        GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
        int textureID = texture.getTextureID();
        textures.add(textureID);
        return textureID;
    }

    public TextFont loadFont(String name) {
        // try returning already loaded font
        TextFont loadedFont = loadedFonts.get(name);
        if (loadedFont != null) {
            return loadedFont;
        }

        // actually load font
        String directoryName = String.format("%s%s/", Loader.FONT_DIRECTORY, name);
        ArrayList<FontChar> chars = new ArrayList<>();
        int size = 0, lineHeight = 0, base = 0, pages = 0, textureWidth = 0, textureHeight = 0;
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(directoryName + "output.fnt"))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                ArrayList<String> parts = new ArrayList<>();
                for (String string : line.split(" ")) {
                    if (!string.isEmpty()) {
                        parts.add(string);
                    }
                }
                if (parts.isEmpty()) {
                    continue;
                }
                String lineType = parts.get(0);
                HashMap<String, Object> properties = new HashMap<>();
                for (int i = 1; i < parts.size(); i++) {
                    String[] keyValue = parts.get(i).split("=");
                    if (keyValue.length != 2) {
                        continue;
                    }
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
                    case "info" -> {
                        size = Math.abs((int) properties.get("size"));
                    }
                    case "common" -> {
                        lineHeight = (int) properties.get("lineHeight");
                        base = (int) properties.get("base");
                        pages = (int) properties.get("pages");
                        textureWidth = (int) properties.get("scaleW");
                        textureHeight = (int) properties.get("scaleH");
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(String.format("Could not load font \"%s\"!\n", name));
        }

        if (chars.size() > UIRenderMaster.MAX_FONT_CHARS) {
            final String baseString = "Font \"%s\" has too many characters (%d). Maximum is %d.";
            throw new RuntimeException(String.format(baseString, chars.size(), UIRenderMaster.MAX_FONT_CHARS));
        }
        if (pages > UIRenderMaster.MAX_FONT_ATLASSES) {
            final String baseString = "Font \"%s\" has too many texture atlasses (%d). Maximum is %d.";
            throw new RuntimeException(String.format(baseString, name, pages, UIRenderMaster.MAX_FONT_ATLASSES));
        }

        int[] textureIDs = new int[pages];
        for (int i = 0; i < pages; i++) {
            textureIDs[i] = loadTexture(String.format("%soutput_%d.png", directoryName, i));
        }

        TextFont font = new TextFont(name, size, lineHeight, base, textureIDs, textureWidth, textureHeight);
        font.loadChars(chars);
        loadedFonts.put(name, font);

        // load ubo
        FloatBuffer uboData = BufferUtils.createFloatBuffer(UIRenderMaster.MAX_FONT_CHARS * 4);
        FontChar[] fontChars = font.getFontChars();
        for (int i = 0; i < fontChars.length; i++) {
            uboData.put(fontChars[i].x() + font.getTextureWidth() * fontChars[i].page());
            uboData.put(fontChars[i].y());
            uboData.put(fontChars[i].width());
            uboData.put(fontChars[i].height());
        }
        font.setUBOData(uboData);

        return font;
    }

    public static void createFontAtlas(String name, int textSize) {
        // doesn't work, don't know why
        // delete old files
        // ArrayList<String> deleteArgs = new ArrayList<>();
        // deleteArgs.add("rm");
        // deleteArgs.add("output*");
        // MainApp.runCommand(String.format("%s%s", FONT_DIRECTORY, name), deleteArgs);

        String directoryName = String.format("%s%s/", FONT_DIRECTORY, name);
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
        commands.add("/home/simon/code/executables/fontbm/fontbm");
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

    public void cleanUp() {
        for (int textureID : textures) {
            GL11.glDeleteTextures(textureID);
        }

        for (int fbo : fbos) {
            GL30.glDeleteFramebuffers(fbo);
        }
    }

    public void tempCleanUp() {
        for (int vaoID : vaos) {
            GL30.glDeleteVertexArrays(vaoID);
        }
        for (int vboID : vbos) {
            GL15.glDeleteBuffers(vboID);
        }
        vaos.clear();
        vbos.clear();
    }
}