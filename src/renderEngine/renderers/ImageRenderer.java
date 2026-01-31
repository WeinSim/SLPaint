package renderEngine.renderers;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;

import renderEngine.Loader;
import renderEngine.bufferobjects.VBOData;
import renderEngine.bufferobjects.VBOFloatData;
import renderEngine.bufferobjects.VBOIntData;
import renderEngine.bufferobjects.VBOMatrixData;
import renderEngine.drawcalls.ImageDrawCall;

public class ImageRenderer extends GeometryShapeRenderer<ImageDrawCall> {

    private static final int MAX_TEXTURE_UNITS = GL11.glGetInteger(GL20.GL_MAX_TEXTURE_IMAGE_UNITS);

    // not sure if binding point is the correct name
    private static final int[] TEXTURE_BINDING_POINTS = {
            GL13.GL_TEXTURE0, GL13.GL_TEXTURE1, GL13.GL_TEXTURE2, GL13.GL_TEXTURE3,
            GL13.GL_TEXTURE4, GL13.GL_TEXTURE5, GL13.GL_TEXTURE6, GL13.GL_TEXTURE7,
            GL13.GL_TEXTURE8, GL13.GL_TEXTURE9, GL13.GL_TEXTURE10, GL13.GL_TEXTURE11,
            GL13.GL_TEXTURE12, GL13.GL_TEXTURE13, GL13.GL_TEXTURE14, GL13.GL_TEXTURE15,
            GL13.GL_TEXTURE16, GL13.GL_TEXTURE17, GL13.GL_TEXTURE18, GL13.GL_TEXTURE19,
            GL13.GL_TEXTURE20, GL13.GL_TEXTURE21, GL13.GL_TEXTURE22, GL13.GL_TEXTURE23,
            GL13.GL_TEXTURE24, GL13.GL_TEXTURE25, GL13.GL_TEXTURE26, GL13.GL_TEXTURE27,
            GL13.GL_TEXTURE28, GL13.GL_TEXTURE29, GL13.GL_TEXTURE30, GL13.GL_TEXTURE31
    };

    private ArrayList<Integer> textureIDs;

    public ImageRenderer(Loader loader) {
        super("image", loader);

        textureIDs = new ArrayList<>();
    }

    @Override
    public void addShape(ImageDrawCall drawCall) {
        // determine sampler ID
        int samplerID = -1;
        for (int i = 0; i < textureIDs.size(); i++) {
            if (drawCall.textureID == textureIDs.get(i)) {
                samplerID = i;
                break;
            }
        }
        if (samplerID == -1) {
            samplerID = textureIDs.size();
            if (samplerID < MAX_TEXTURE_UNITS) {
                textureIDs.add(drawCall.textureID);
            } else {
                // silently fail.
                // for a proper implementation, the UBO array length for images shouöd be
                // limited to MAX_TEXTURE_UNITS.
                System.err.println("Maximum number of image texture used. Unable to draw image");
                return;
            }
        }
        drawCall.samplerID = samplerID;

        super.addShape(drawCall);
    }

    @Override
    public void finish() {
        textureIDs.clear();
    }

    @Override
    protected VBOData[] getVBOs(ArrayList<Batch> batches, int vertexCount) {
        VBOIntData dataIndex = new VBOIntData(vertexCount, 1);
        VBOMatrixData transformationMatrix = new VBOMatrixData(vertexCount, 3);
        VBOFloatData position = new VBOFloatData(vertexCount, 2);
        VBOFloatData depth = new VBOFloatData(vertexCount, 1);
        VBOFloatData size = new VBOFloatData(vertexCount, 2);

        int batchIndex = 0;
        for (Batch batch : batches) {
            for (ImageDrawCall drawCall : batch.getDrawCalls()) {
                dataIndex.putData(batchIndex);
                transformationMatrix.putData(drawCall.uiMatrix);
                position.putData(drawCall.position);
                depth.putData(drawCall.depth);
                size.putData(drawCall.size);
            }
            batchIndex++;
        }

        return new VBOData[] {
                dataIndex, transformationMatrix, position, depth, size
        };
    }

    @Override
    public void prepare() {
        for (int i = 0; i < textureIDs.size(); i++) {
            GL13.glActiveTexture(TEXTURE_BINDING_POINTS[i]);
            int textureID = textureIDs.get(i);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);

            String name = String.format("textureSamplers[%d]", i);
            shaderProgram.loadUniform(name, i);
        }

        // GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
    }
}