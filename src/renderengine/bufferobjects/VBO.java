package renderengine.bufferobjects;

import org.lwjgl.opengl.GL15;

public abstract class VBO {

    protected final int vboID;

    public VBO() {
        vboID = GL15.glGenBuffers();
    }

    public void cleanUp() {
        GL15.glDeleteBuffers(vboID);
    }
}