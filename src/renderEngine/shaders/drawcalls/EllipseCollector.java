package renderEngine.shaders.drawcalls;

import java.util.ArrayList;

import org.lwjglx.util.vector.Vector4f;

import renderEngine.shaders.bufferobjects.VBOData;
import renderEngine.shaders.bufferobjects.VBOFloatData;
import renderEngine.shaders.bufferobjects.VBOIntData;
import renderEngine.shaders.bufferobjects.VBOMatrixData;

public class EllipseCollector extends ShapeCollector<EllipseDrawCall> {

    public EllipseCollector() {
        super(1, "EllipseData", "ellipse",
                new String[] { "dataIndex", "transformationMatrix", "position", "depth", "size", "color" },
                new int[] { 1, 3, 1, 1, 1, 1 });
    }

    @Override
    protected VBOData[] getVBOs(ArrayList<Batch> batches, int vertexCount) {
        VBOIntData dataIndex = new VBOIntData(vertexCount, 1);
        VBOMatrixData transformationMatrix = new VBOMatrixData(vertexCount, 3);
        VBOFloatData position = new VBOFloatData(vertexCount, 2);
        VBOFloatData depth = new VBOFloatData(vertexCount, 1);
        VBOFloatData size = new VBOFloatData(vertexCount, 2);
        VBOFloatData color = new VBOFloatData(vertexCount, 4);

        int batchIndex = 0;
        for (Batch batch : batches) {
            for (EllipseDrawCall drawCall : batch.getDrawCalls()) {
                dataIndex.putData(batchIndex);
                transformationMatrix.putData(drawCall.uiMatrix);
                position.putData(drawCall.position);
                depth.putData(drawCall.depth);
                size.putData(drawCall.size);
                color.putData(new Vector4f(
                        drawCall.color.x,
                        drawCall.color.y,
                        drawCall.color.z,
                        drawCall.color.w));
            }
            batchIndex++;
        }

        return new VBOData[] { dataIndex, transformationMatrix, position, depth, size, color };
    }
}