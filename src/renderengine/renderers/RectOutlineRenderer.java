package renderengine.renderers;

import java.util.ArrayList;

import renderengine.Loader;
import renderengine.bufferobjects.FloatVBO;
import renderengine.bufferobjects.IntVBO;
import renderengine.bufferobjects.MatrixVBO;
import renderengine.drawcalls.RectOutlineDrawCall;

public class RectOutlineRenderer extends GeometryShapeRenderer<RectOutlineDrawCall> {

    public RectOutlineRenderer(Loader loader) {
        super("rectOutline", loader);
    }

    @Override
    protected void loadVBOs(ArrayList<Batch> batches, int vertexCount) {
        IntVBO dataIndex = model.getIntVBO("dataIndex");
        MatrixVBO transformationMatrix = model.getMatrixVBO("transformationMatrix");
        FloatVBO position = model.getFloatVBO("position");
        FloatVBO depth = model.getFloatVBO("depth");
        FloatVBO size = model.getFloatVBO("size");
        FloatVBO color1 = model.getFloatVBO("color1");
        FloatVBO strokeWeight = model.getFloatVBO("strokeWeight");

        int batchIndex = 0;
        for (Batch batch : batches) {
            for (RectOutlineDrawCall drawCall : batch.getDrawCalls()) {
                dataIndex.putData(batchIndex);
                transformationMatrix.putData(drawCall.uiMatrix);
                position.putData(drawCall.position);
                depth.putData(drawCall.depth);
                size.putData(drawCall.size);
                color1.putData(drawCall.color1);
                strokeWeight.putData(drawCall.strokeWeight);
            }
            batchIndex++;
        }
    }
}