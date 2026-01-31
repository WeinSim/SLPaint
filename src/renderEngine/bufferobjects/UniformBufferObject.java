package renderEngine.bufferobjects;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL31;

import renderEngine.Loader;

public class UniformBufferObject {

    public static final int UBO_ARRAY_LENGTH = 256;

    private final String name;
    private final int binding;

    private ByteBuffer data;
    private boolean synced;

    private int bufferID = 0;

    public UniformBufferObject(String name, int binding) {
        this.name = name;
        this.binding = binding;

        synced = false;
    }

    public void setData(ByteBuffer data) {
        if (this.data == data)
            return;

        this.data = data;
        synced = false;
    }

    public void syncData(Loader loader) {
        GL31.glBindBufferBase(GL31.GL_UNIFORM_BUFFER, binding, bufferID);

        if (synced)
            return;

        loader.loadToUBO(this, data);
        synced = true;
    }

    public int getBufferID() {
        return bufferID;
    }

    public void setBufferID(int bufferID) {
        this.bufferID = bufferID;
    }

    public String getName() {
        return name;
    }

    public int getBinding() {
        return binding;
    }
}