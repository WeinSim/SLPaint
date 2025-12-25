package renderEngine.shaders.bufferobjects;

import java.nio.FloatBuffer;

import renderEngine.Loader;

/**
 * Represents both a uniform block as well as a uniform buffer object.
 * 
 * Each UBO is expected to contain some number of vec4 arrays, each of which is
 * exactly {@code UBO_ARRAY_LENGTH} elements long.
 */
public class UniformBufferObject {

    public static final int UBO_ARRAY_LENGTH = 256;

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