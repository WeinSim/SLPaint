package renderEngine.shaders.drawcalls;

import java.util.ArrayList;

import renderEngine.shaders.bufferobjects.VBOData;
import renderEngine.shaders.bufferobjects.VBOFloatData;
import renderEngine.shaders.bufferobjects.VBOIntData;
import renderEngine.shaders.bufferobjects.VBOMatrixData;

public class HSLCollector extends ShapeCollector<HSLDrawCall> {

    public HSLCollector() {
        super(1, "HSLData", "hsl",
                new String[] { "dataIndex", "transformationMatrix", "position", "depth", "size", "color", "hueSatAlpha",
                        "hsv", "orientation" },
                new int[] { 1, 3, 1, 1, 1, 1, 1, 1, 1, 1, 1 });
    }

    @Override
    protected VBOData[] getVBOs(ArrayList<Batch> batches, int vertexCount) {
        VBOIntData dataIndex = new VBOIntData(vertexCount, 1);
        VBOMatrixData transformationMatrix = new VBOMatrixData(vertexCount, 3);
        VBOFloatData position = new VBOFloatData(vertexCount, 2);
        VBOFloatData depth = new VBOFloatData(vertexCount, 1);
        VBOFloatData size = new VBOFloatData(vertexCount, 2);

        VBOFloatData color = new VBOFloatData(vertexCount, 3);
        VBOIntData hueSatAlpha = new VBOIntData(vertexCount, 1);
        VBOIntData hsv = new VBOIntData(vertexCount, 1);
        VBOIntData orientation = new VBOIntData(vertexCount, 1);

        int batchIndex = 0;
        for (Batch batch : batches) {
            for (HSLDrawCall drawCall : batch.getDrawCalls()) {
                dataIndex.putData(batchIndex);
                transformationMatrix.putData(drawCall.uiMatrix);
                position.putData(drawCall.position);
                depth.putData(drawCall.depth);
                size.putData(drawCall.size);

                color.putData(drawCall.color);
                hueSatAlpha.putData(drawCall.hueSatAlpha);
                hsv.putData(drawCall.hsv);
                orientation.putData(drawCall.orientation);
            }
            batchIndex++;
        }

        return new VBOData[] {
                dataIndex, transformationMatrix, position, depth, size, color, hueSatAlpha, hsv, orientation
        };
    }
}