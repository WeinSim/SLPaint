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
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

import main.apps.MainApp;
import renderEngine.fonts.FontChar;
import renderEngine.fonts.TextFont;
import sutil.SUtil;

public class Loader {

    // private App app;

    private ArrayList<Integer> vaos;
    private ArrayList<Integer> vbos;
    private ArrayList<Integer> textures;
    private ArrayList<Integer> fbos;

    private boolean textMode;
    private ArrayList<Integer> tempVAOs;
    private ArrayList<Integer> tempVBOs;

    private HashMap<String, TextFont> loadedFonts;

    public Loader() {
        // this.app = app;

        vaos = new ArrayList<>();
        vbos = new ArrayList<>();
        textures = new ArrayList<>();
        fbos = new ArrayList<>();

        tempVAOs = new ArrayList<>();
        tempVBOs = new ArrayList<>();

        loadedFonts = new HashMap<>();

        textMode = false;
    }

    public RawModel loadToVAO(double[] positions) {
        textMode = false;
        int vaoID = createVAO();
        storeDataInAttributeList(0, 2, positions);
        unbindVAO();
        return new RawModel(vaoID, positions.length / 2);
    }

    public RawModel loadToTextVAO(double[] positions, double[] textureCoords, double[] sizes) {
        textMode = true;
        int vaoID = createVAO();
        storeDataInAttributeList(0, 2, positions);
        storeDataInAttributeList(1, 2, textureCoords);
        storeDataInAttributeList(2, 2, sizes);
        unbindVAO();
        return new RawModel(vaoID, positions.length);
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
            GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
        } catch (Exception e) {
            System.out.format("Could not load texture \"%s\"!\n", path);
            e.printStackTrace();
            System.exit(-1);
        }
        int textureID = texture.getTextureID();
        textures.add(textureID);
        return textureID;
    }

    public TextFont loadFont(String name, int textSize, boolean generate) {
        // try returning already loaded font
        TextFont loadedFont = loadedFonts.get(name);
        if (loadedFont != null) {
            return loadedFont;
        }

        // actually load font
        String directoryName = String.format("res/fonts/%s/", name);
        if (generate) {
            MainApp.runCommand(directoryName,
                    getFontGenerationCommand(name, 2, UIRenderMaster.FONT_TEXTURE_WIDTH,
                            UIRenderMaster.FONT_TEXTURE_HEIGHT, textSize, new int[] { 32, 126, 160, 255 },
                            new int[] { 9633 }, 0));
        }
        ArrayList<FontChar> chars = new ArrayList<>();
        int size = 0, lineHeight = 0, base = 0;
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
                    case "char" -> {
                        FontChar newChar = new FontChar();
                        newChar.id = (int) properties.get("id");
                        newChar.x = (int) properties.get("x");
                        newChar.y = (int) properties.get("y");
                        newChar.width = (int) properties.get("width");
                        newChar.height = (int) properties.get("height");
                        newChar.xOffset = (int) properties.get("xoffset");
                        newChar.yOffset = (int) properties.get("yoffset");
                        newChar.xAdvance = (int) properties.get("xadvance");
                        chars.add(newChar);
                    }
                    case "info" -> {
                        size = Math.abs((int) properties.get("size"));
                    }
                    case "common" -> {
                        lineHeight = (int) properties.get("lineHeight");
                        base = (int) properties.get("base");
                    }
                }
            }
        } catch (IOException e) {
            System.out.format("Could not load font \"%s\"!\n", name);
            System.exit(1);
        }

        int textureID = loadTexture(directoryName + "output_0.png");

        // TextFont font = new TextFont(app, name, size, lineHeight, base, texture);
        TextFont font = new TextFont(name, size, lineHeight, base, textureID);
        font.loadChars(chars);
        loadedFonts.put(name, font);

        return font;
    }

    private ArrayList<String> getFontGenerationCommand(String fontName, int padding, int textureWidth,
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

    private void addArgument(ArrayList<String> commands, String argument, int value) {
        addArgument(commands, argument, Integer.toString(value));
    }

    private void addArgument(ArrayList<String> commands, String argument, String value) {
        commands.add("--" + argument);
        commands.add(value);
    }

    public void cleanUp() {
        cleanUp(vaos, vbos);
        cleanUp(tempVAOs, tempVBOs);

        for (int textureID : textures) {
            GL11.glDeleteTextures(textureID);
        }

        for (int fbo : fbos) {
            GL30.glDeleteFramebuffers(fbo);
        }
    }

    public void tempCleanUp() {
        cleanUp(tempVAOs, tempVBOs);
    }

    private void cleanUp(ArrayList<Integer> vaos, ArrayList<Integer> vbos) {
        for (int vaoID : vaos) {
            GL30.glDeleteVertexArrays(vaoID);
        }
        for (int vboID : vbos) {
            GL15.glDeleteBuffers(vboID);
        }
        vaos.clear();
        vbos.clear();
    }

    private int createVAO() {
        int vaoID = GL30.glGenVertexArrays();
        (textMode ? tempVAOs : vaos).add(vaoID);
        GL30.glBindVertexArray(vaoID);
        return vaoID;
    }

    private void storeDataInAttributeList(int attributeNumber, int coordinateSize, double[] data) {
        int vboID = GL15.glGenBuffers();
        (textMode ? tempVBOs : vbos).add(vboID);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);
        FloatBuffer buffer = storeDataInFloatBuffer(data);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(attributeNumber, coordinateSize, GL11.GL_FLOAT, false, 0, 0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    private void unbindVAO() {
        GL30.glBindVertexArray(0);
    }

    // private void bindIndicesBuffer(int[] indices) {
    // int vboID = GL15.glGenBuffers();
    // (textMode? textVBOs : vbos).add(vboID);
    // GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboID);
    // IntBuffer buffer = storeDataInIntBuffer(indices);
    // GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
    // }

    // private IntBuffer storeDataInIntBuffer(int[] data) {
    // IntBuffer buffer = BufferUtils.createIntBuffer(data.length);
    // buffer.put(data);
    // buffer.flip();
    // return buffer;
    // }

    private FloatBuffer storeDataInFloatBuffer(double[] data) {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
        float[] floatData = new float[data.length];
        for (int i = 0; i < floatData.length; i++) {
            floatData[i] = (float) data[i];
        }
        buffer.put(floatData);
        buffer.flip();
        return buffer;
    }
}