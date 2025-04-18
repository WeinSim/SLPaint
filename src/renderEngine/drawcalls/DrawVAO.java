package renderEngine.drawcalls;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;

public abstract class DrawVAO<C extends DrawCall, D extends DrawData> {

    private static final int MAX_DRAW_DATA = 256;

    private ArrayList<C> drawCalls;

    private ArrayList<D> drawDataList;

    private int vertexCount;

    public DrawVAO() {
        drawCalls = new ArrayList<>();

        drawDataList = new ArrayList<>(MAX_DRAW_DATA);
    }

    public int getDataIndex(D textData) {
        for (int i = 0; i < drawDataList.size(); i++) {
            if (drawDataList.get(i).equals(textData)) {
                return i;
            }
        }
        return -1;
    }

    public boolean hasRemainingCapacity() {
        return drawDataList.size() < MAX_DRAW_DATA;
    }

    public void addDrawCall(C drawCall, D drawData) {
        addDrawCall(drawCall, drawDataList.size());
        drawDataList.add(drawData);
    }

    public void addDrawCall(C drawCall, int dataIndex) {
        vertexCount += getVertexCount(drawCall);
        drawCall.setDataIndex(dataIndex);
        drawCalls.add(drawCall);
    }

    protected abstract int getVertexCount(C drawCall);

    public FloatBuffer getUBOData() {
        float[][] uboData = new float[drawDataList.size()][];
        int index = 0;
        for (D textData : drawDataList) {
            uboData[index++] = textData.getUBOData();
        }

        FloatBuffer buffer = BufferUtils.createFloatBuffer(8 * MAX_DRAW_DATA);
        for (int j = 0; j < 2; j++) {
            for (int i = 0; i < uboData.length; i++) {
                buffer.put(uboData[i][4 * j]);
                buffer.put(uboData[i][4 * j + 1]);
                buffer.put(uboData[i][4 * j + 2]);
                buffer.put(uboData[i][4 * j + 3]);
            }
            for (int i = 0; i < MAX_DRAW_DATA - drawDataList.size(); i++) {
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