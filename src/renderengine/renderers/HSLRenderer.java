package renderengine.renderers;

import java.util.ArrayList;

import renderengine.Loader;
import renderengine.bufferobjects.FloatVBO;
import renderengine.bufferobjects.IntVBO;
import renderengine.bufferobjects.MatrixVBO;
import renderengine.drawcalls.HSLDrawCall;

public class HSLRenderer extends GeometryShapeRenderer<HSLDrawCall> {

    public HSLRenderer(Loader loader) {
        super("hsl", loader);
    }

    @Override
    protected void loadVBOs(ArrayList<Batch> batches, int vertexCount) {
        IntVBO dataIndex = model.getIntVBO("dataIndex");
        MatrixVBO transformationMatrix = model.getMatrixVBO("transformationMatrix");
        FloatVBO position = model.getFloatVBO("position");
        FloatVBO depth = model.getFloatVBO("depth");
        FloatVBO size = model.getFloatVBO("size");

        FloatVBO color = model.getFloatVBO("color");
        IntVBO flags = model.getIntVBO("flags");

        int batchIndex = 0;
        for (Batch batch : batches) {
            for (HSLDrawCall drawCall : batch.getDrawCalls()) {
                dataIndex.putData(batchIndex);
                transformationMatrix.putData(drawCall.uiMatrix);
                position.putData(drawCall.position);
                depth.putData(drawCall.depth);
                // depth.putData(0.8f);
                size.putData(drawCall.size);

                color.putData(drawCall.color);
                flags.putData(drawCall.flags);
            }
            batchIndex++;
        }
    }
}