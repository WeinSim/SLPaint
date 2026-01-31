package renderEngine.renderers;

import java.util.ArrayList;

import renderEngine.Loader;
import renderEngine.bufferobjects.VBOData;
import renderEngine.bufferobjects.VBOFloatData;
import renderEngine.bufferobjects.VBOIntData;
import renderEngine.bufferobjects.VBOMatrixData;
import renderEngine.drawcalls.RectFillDrawCall;

public class RectFillRenderer extends GeometryShapeRenderer<RectFillDrawCall> {

    public RectFillRenderer(Loader loader) {
        super("rectFill", loader);
    }

    @Override
    protected VBOData[] getVBOs(ArrayList<Batch> batches, int vertexCount) {
        VBOIntData dataIndex = new VBOIntData(vertexCount, 1);
        VBOMatrixData transformationMatrix = new VBOMatrixData(vertexCount, 3);
        VBOFloatData position = new VBOFloatData(vertexCount, 2);
        VBOFloatData depth = new VBOFloatData(vertexCount, 1);
        VBOFloatData size = new VBOFloatData(vertexCount, 2);
        VBOFloatData color1 = new VBOFloatData(vertexCount, 4);

        int batchIndex = 0;
        for (Batch batch : batches) {
            for (RectFillDrawCall drawCall : batch.getDrawCalls()) {
                dataIndex.putData(batchIndex);
                transformationMatrix.putData(drawCall.uiMatrix);
                position.putData(drawCall.position);
                depth.putData(drawCall.depth);
                size.putData(drawCall.size);
                color1.putData(drawCall.color1);
            }
            batchIndex++;
        }

        return new VBOData[] {
                dataIndex, transformationMatrix, position, depth, size, color1
        };
    }
}