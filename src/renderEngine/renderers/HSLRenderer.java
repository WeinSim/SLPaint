package renderEngine.renderers;

import java.util.ArrayList;

import renderEngine.Loader;
import renderEngine.bufferobjects.VBOData;
import renderEngine.bufferobjects.VBOFloatData;
import renderEngine.bufferobjects.VBOIntData;
import renderEngine.bufferobjects.VBOMatrixData;
import renderEngine.drawcalls.HSLDrawCall;

public class HSLRenderer extends GeometryShapeRenderer<HSLDrawCall> {

    public HSLRenderer(Loader loader) {
        super("hsl", loader);
    }

    @Override
    protected VBOData[] getVBOs(ArrayList<Batch> batches, int vertexCount) {
        VBOIntData dataIndex = new VBOIntData(vertexCount, 1);
        VBOMatrixData transformationMatrix = new VBOMatrixData(vertexCount, 3);
        VBOFloatData position = new VBOFloatData(vertexCount, 2);
        VBOFloatData depth = new VBOFloatData(vertexCount, 1);
        VBOFloatData size = new VBOFloatData(vertexCount, 2);

        VBOFloatData color = new VBOFloatData(vertexCount, 3);
        VBOIntData flags = new VBOIntData(vertexCount, 1);

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

        return new VBOData[] {
                dataIndex, transformationMatrix, position, depth, size, color, flags
        };
    }
}