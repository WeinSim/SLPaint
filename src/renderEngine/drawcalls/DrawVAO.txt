package renderEngine.drawcalls;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.function.Function;

import org.lwjgl.BufferUtils;

import renderEngine.shaders.UniformBufferObject;

public class DrawVAO<C extends DrawCall, D extends DrawData> {

    private ArrayList<C> drawCalls;

    private ArrayList<D> drawDataList;

    private final Function<C, Integer> vertexCountFunction;
    private int vertexCount;

    public DrawVAO(Function<C, Integer> vertexCountGetter) {
        this.vertexCountFunction = vertexCountGetter;

        drawCalls = new ArrayList<>();

        drawDataList = new ArrayList<>(UniformBufferObject.UBO_ARRAY_LENGTH);
    }

    public int getDataIndex(D drawData) {
        for (int i = 0; i < drawDataList.size(); i++) {
            if (drawDataList.get(i).equals(drawData)) {
                return i;
            }
        }
        return -1;
    }

    public boolean hasRemainingCapacity() {
        return drawDataList.size() < UniformBufferObject.UBO_ARRAY_LENGTH;
    }

    public void addDrawCall(C drawCall, D drawData) {
        addDrawCall(drawCall, drawDataList.size());
        drawDataList.add(drawData);
    }

    public void addDrawCall(C drawCall, int dataIndex) {
        vertexCount += vertexCountFunction.apply(drawCall);
        drawCall.setDataIndex(dataIndex);
        drawCalls.add(drawCall);
    }

    public FloatBuffer getUBOData() {
        float[][] uboData = new float[drawDataList.size()][];
        int index = 0;
        int numUBOArrays = 0;
        for (D drawData : drawDataList) {
            if (numUBOArrays == 0) {
                numUBOArrays = drawData.getNumUBOArrays();
            }
            uboData[index++] = drawData.getUBOData();
        }

        FloatBuffer buffer = BufferUtils.createFloatBuffer(numUBOArrays * 4 * UniformBufferObject.UBO_ARRAY_LENGTH);
        for (int j = 0; j < numUBOArrays; j++) {
            for (int i = 0; i < uboData.length; i++) {
                buffer.put(uboData[i][4 * j]);
                buffer.put(uboData[i][4 * j + 1]);
                buffer.put(uboData[i][4 * j + 2]);
                buffer.put(uboData[i][4 * j + 3]);
            }
            for (int i = 0; i < UniformBufferObject.UBO_ARRAY_LENGTH - drawDataList.size(); i++) {
                buffer.put(0f);
                buffer.put(0f);
                buffer.put(0f);
                buffer.put(0f);
            }
        }
        buffer.flip();
        return buffer;
    }

    public ArrayList<C> getDrawCalls() {
        return drawCalls;
    }

    public int getVertexCount() {
        return vertexCount;
    }
}