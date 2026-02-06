package renderengine;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;

import main.apps.MainApp;
import renderengine.bufferobjects.FrameBufferObject;
import renderengine.bufferobjects.UniformBufferObject;
import sutil.SUtil;

public class Loader {

    public static final String FONT_DIRECTORY = "res/fonts/";

    private ArrayList<Integer> vaos;
    private ArrayList<Integer> vbos;

    private ArrayList<Integer> textures;
    private ArrayList<Integer> fbos;

    public Loader() {
        vaos = new ArrayList<>();
        vbos = new ArrayList<>();
        textures = new ArrayList<>();
        fbos = new ArrayList<>();
    }

    public void loadToUBO(UniformBufferObject ubo, ByteBuffer data) {
        int bufferID = ubo.getBufferID();

        if (bufferID == 0) {
            bufferID = GL15.glGenBuffers();
            ubo.setBufferID(bufferID);
        }

        GL15.glBindBuffer(GL31.GL_UNIFORM_BUFFER, bufferID);
        GL15.glBufferData(GL31.GL_UNIFORM_BUFFER, data, GL15.GL_DYNAMIC_DRAW);
        GL15.glBindBuffer(GL31.GL_UNIFORM_BUFFER, 0);
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
        // GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER,
        // GL30.GL_LINEAR_MIPMAP_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL30.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        // unbind texture
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        // attach texture to framebuffer
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, texture, 0);

        return new FrameBufferObject(fbo, texture, width, height);
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

    public void cleanUpFBO(FrameBufferObject fbo) {
        int fboID = fbo.fboID();
        GL30.glDeleteFramebuffers(fboID);
        fbos.remove((Integer) fboID);

        int textureID = fbo.textureID();
        GL11.glDeleteTextures(textureID);
        textures.remove((Integer) textureID);
    }
}