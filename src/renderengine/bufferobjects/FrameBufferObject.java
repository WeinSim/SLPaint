package renderengine.bufferobjects;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

public class FrameBufferObject implements Cleanable {

    public final int fboID;
    public final int textureID;
    public final int width;
    public final int height;

    public FrameBufferObject(int width, int height) {
        this.width = width;
        this.height = height;

        // https://learnopengl.com/Advanced-OpenGL/Framebuffers

        // create framebuffer
        fboID = GL30.glGenFramebuffers();
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fboID);

        // create texture
        textureID = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE,
                (int[]) null);
        // GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER,
        // GL30.GL_LINEAR_MIPMAP_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL30.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        // unbind texture
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        // attach texture to framebuffer
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, textureID, 0);
    }

    @Override
    public void cleanUp() {
        GL30.glDeleteFramebuffers(fboID);
        GL11.glDeleteTextures(textureID);
    }
}