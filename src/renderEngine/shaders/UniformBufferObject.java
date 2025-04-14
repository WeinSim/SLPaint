package renderEngine.shaders;

import java.nio.FloatBuffer;

import renderEngine.Loader;

/**
 * Represents both a uniform block as well as a uniform buffer object
 */
public class UniformBufferObject {

    private final String name;
    private final int binding;

    private FloatBuffer data;
    private boolean synced;

    private int bufferID = 0;

    public UniformBufferObject(String name, int binding) {
        this.name = name;
        this.binding = binding;
        synced = false;
    }

    public int getBufferID() {
        return bufferID;
    }

    public void setBufferID(int bufferID) {
        this.bufferID = bufferID;
    }

    public FloatBuffer getData() {
        return data;
    }

    public void setData(FloatBuffer data) {
        if (this.data == data) {
            return;
        }
        this.data = data;
        synced = false;
    }

    public void syncData(Loader loader) {
        if (synced) {
            return;
        }

        loader.loadToUBO(this, data);
        synced = true;
    }

    public String getName() {
        return name;
    }

    public int getBinding() {
        return binding;
    }
}