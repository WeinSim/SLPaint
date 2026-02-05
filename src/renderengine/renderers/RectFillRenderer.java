package renderengine.renderers;

import java.util.ArrayList;

import org.lwjglx.util.vector.Vector2f;

import renderengine.Loader;
import renderengine.bufferobjects.FloatVBO;
import renderengine.bufferobjects.IntVBO;
import renderengine.bufferobjects.MatrixVBO;
import renderengine.drawcalls.RectFillDrawCall;

public class RectFillRenderer extends InstanceShapeRenderer<RectFillDrawCall> {

    public RectFillRenderer(Loader loader) {
        super("rectFill", loader);
    }

    @Override
    protected void loadVBOs(ArrayList<Batch> batches, int vertexCount) {
        // IntVBO dataIndex = model.getIntVBO("dataIndex");
        IntVBO dataIndex = model.getIntVBO("gIndex");
        MatrixVBO transformationMatrix = model.getMatrixVBO("transformationMatrix");
        FloatVBO position = model.getFloatVBO("position");
        FloatVBO depth = model.getFloatVBO("depth");
        FloatVBO size = model.getFloatVBO("size");
        FloatVBO color1 = model.getFloatVBO("color1_in");

        int i = 0;
        for (Batch batch : batches) {
            for (RectFillDrawCall drawCall : batch.getDrawCalls()) {
                dataIndex.putData(i);
                transformationMatrix.putData(drawCall.uiMatrix);
                position.putData(drawCall.position);
                depth.putData(drawCall.depth);
                size.putData(drawCall.size);
                color1.putData(drawCall.color1);
            }
            i++;
        }
    }

    @Override
    protected void initRawModel() {
        model.initVertexVBOs(4);

        FloatVBO cornerPos = model.getFloatVBO("cornerPos");
        cornerPos.putData(new Vector2f(0f, 0f));
        cornerPos.putData(new Vector2f(1f, 0f));
        cornerPos.putData(new Vector2f(0f, 1f));
        cornerPos.putData(new Vector2f(1f, 1f));
        
        model.finishVertexVBOs();
    }
}