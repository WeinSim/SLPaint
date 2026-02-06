package renderengine.bufferobjects;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL31;

public class UniformBufferObject implements Cleanable {

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

    public void syncData() {
        GL31.glBindBufferBase(GL31.GL_UNIFORM_BUFFER, binding, bufferID);

        if (synced)
            return;

        if (bufferID == 0)
            bufferID = GL15.glGenBuffers();
        GL15.glBindBuffer(GL31.GL_UNIFORM_BUFFER, bufferID);
        GL15.glBufferData(GL31.GL_UNIFORM_BUFFER, data, GL15.GL_DYNAMIC_DRAW);
        GL15.glBindBuffer(GL31.GL_UNIFORM_BUFFER, 0);

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

    @Override
    public void cleanUp() {
        GL15.glDeleteBuffers(bufferID);
    }
}