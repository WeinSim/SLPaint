package renderengine.renderers;

import java.util.ArrayList;

import org.lwjglx.util.vector.Vector4f;

import renderengine.Loader;
import renderengine.bufferobjects.FloatVBO;
import renderengine.bufferobjects.IntVBO;
import renderengine.bufferobjects.MatrixVBO;
import renderengine.drawcalls.EllipseDrawCall;

public class EllipseRenderer extends GeometryShapeRenderer<EllipseDrawCall> {

    public EllipseRenderer(Loader loader) {
        super("ellipse", loader);
    }

    @Override
    protected void loadVBOs(ArrayList<Batch> batches, int vertexCount) {
        IntVBO dataIndex = model.getIntVBO("dataIndex");
        MatrixVBO transformationMatrix = model.getMatrixVBO("transformationMatrix");
        FloatVBO position = model.getFloatVBO("position");
        FloatVBO depth = model.getFloatVBO("depth");
        FloatVBO size = model.getFloatVBO("size");
        FloatVBO color = model.getFloatVBO("color");

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
    }
}