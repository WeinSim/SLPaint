package renderEngine.drawobjects;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;

import renderEngine.Loader;
import renderEngine.RawModel;
import renderEngine.shaders.ShaderProgram;
import renderEngine.shaders.UniformBufferObject;

public class Renderable<C extends DrawCall> {

    private final Attribute<?, ?>[] vaoAttributes;
    private final Attribute<?, ?>[] uboAttributes;
    private final String uboName;

    private final ShaderProgram shaderProgram;

    private ArrayList<VertexData> vertices;
    private ArrayList<UBOData> uboEntries;

    public Renderable(Attribute<?, ?>[] vaoAttributes, Attribute<?, ?>[] uboAttributes, String uboName, String shaderName) {
        this.vaoAttributes = vaoAttributes;
        this.uboAttributes = uboAttributes;
        this.uboName = uboName;

        shaderProgram = new ShaderProgram(shaderName, /* TODO */ null, true);

        vertices = new ArrayList<>();
        uboEntries = new ArrayList<>();
    }

    public void addDrawCall(C drawCall) {
        AttributeValue[] uboDataArray = new AttributeValue[uboAttributes.length];
        for (int i = 0; i < uboDataArray.length; i++) {
            uboDataArray[i] = drawCall.getAttribute(uboAttributes[i]);
        }
        UBOData newUBO = new UBOData(uboDataArray);
        UBOData existingUBO = getExistingUBO(newUBO);
        if (existingUBO == null) {
            uboEntries.add(newUBO);
            existingUBO = newUBO;
        }

        AttributeValue[] vertexDataArray = new AttributeValue[vaoAttributes.length];
        for (int i = 0; i < vertexDataArray.length; i++) {
            vertexDataArray[i] = drawCall.getAttribute(vaoAttributes[i]);
        }
        vertices.add(new VertexData(vertexDataArray, existingUBO));
    }

    private UBOData getExistingUBO(UBOData newData) {
        for (UBOData uboData : uboEntries) {
            if (uboData.equals(newData)) {
                return uboData;
            }
        }

        return null;
    }

    public RawModel getNextRawModel(Loader loader) {
        if (uboEntries.isEmpty()) {
            return null;
        }

        // TODO: this could be sped up
        // collect the next batch of ubo data
        UBOData[] uboArray = new UBOData[UniformBufferObject.UBO_ARRAY_LENGTH];
        int numUBOs = 0;
        while (numUBOs < uboArray.length && !uboEntries.isEmpty()) {
            UBOData nextUBOData = uboEntries.removeLast();
            nextUBOData.active = true;
            uboArray[numUBOs++] = nextUBOData;
        }

        float[] array = new float[uboAttributes.length * 4 * UniformBufferObject.UBO_ARRAY_LENGTH];
        int index = 0;
        for (int i = 0; i < UniformBufferObject.UBO_ARRAY_LENGTH; i++) {
            if (i < numUBOs) {
                uboArray[i].putData(array, index);
                index += 4;
            } else {
                for (int j = 0; j < uboAttributes.length; j++) {
                    array[index++] = 0.0f;
                    array[index++] = 0.0f;
                    array[index++] = 0.0f;
                    array[index++] = 0.0f;
                }
            }
        }
        FloatBuffer uboBuffer = BufferUtils.createFloatBuffer(uboArray.length);
        uboBuffer.put(array);
        shaderProgram.setUniformBlockData(uboName, uboBuffer);
        shaderProgram.syncUniformBlock(uboName, loader);

        // determine all vertices that point to one of the collected ubos
        ArrayList<VertexData> nextVertices = new ArrayList<>();
        for (VertexData vertexData : vertices) {
            if (vertexData.uboData.active) {
                nextVertices.add(vertexData);
            }
        }

        // TODO: the following code but for general vertex attribute structure
        // double[] positions = new double[nextVertices.size() * 3]
        // ...
        // int[] dataIndices = new int[nextVertices.size()];
        // put data into arrays
        // return loader.loadToVAO(positions, ..., dataIndices);
    }

    public void clear() {
        vertices.clear();
        uboEntries.clear();
    }

    public int getNumVBOs() {
        // +1 for data index
        return vaoAttributes.length + 1;
    }

    public ShaderProgram getShaderProgram() {
        return shaderProgram;
    }

    private static class VertexData {

        AttributeValue[] data;
        UBOData uboData;

        VertexData(AttributeValue[] data, UBOData uboData) {
            this.data = data;
            this.uboData = uboData;
        }
    }

    private static class UBOData {

        AttributeValue[] data;
        // TODO: this field is only used in the getNextRawModel method and doesn't
        // really belong in this class
        boolean active;

        UBOData(AttributeValue[] data) {
            this.data = data;
            active = false;
        }

        /**
         * Two non-null {@code UBOData} objects are equal iff their {@code data} arrays
         * have the same length and their entries are pairwise equal.
         */
        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }

            if (obj instanceof UBOData ubo) {
                if (data.length != ubo.data.length) {
                    return false;
                }
                for (int i = 0; i < data.length; i++) {
                    if (!data[i].equals(ubo.data[i])) {
                        return false;
                    }
                }
                return true;
            }

            return false;
        }

        void putData(float[] array, int index) {
            for (AttributeValue value : data) {
                value.putData(array, index);
            }
        }
    }
}