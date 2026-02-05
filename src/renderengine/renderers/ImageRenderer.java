package renderengine.renderers;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjglx.util.vector.Matrix3f;

import renderengine.Loader;
import renderengine.bufferobjects.FloatVBO;
import renderengine.bufferobjects.IntVBO;
import renderengine.bufferobjects.MatrixVBO;
import renderengine.drawcalls.ImageDrawCall;

public class ImageRenderer extends GeometryShapeRenderer<ImageDrawCall> {

    private static final int NO_TEXTURE = -1;

    /**
     * Texture sampler i will sample from texture textureIDs[i]
     */
    private int[] textureIDs;

    public ImageRenderer(Loader loader) {
        super("image", loader);

        textureIDs = new int[NUM_TEXTURE_UNITS];
        clearTextureIDs();

        shaderProgram.start();
        for (int i = 0; i < NUM_TEXTURE_UNITS; i++) {
            String name = String.format("textureSamplers[%d]", i);
            shaderProgram.loadUniform(name, i);
        }
        shaderProgram.stop();
    }

    @Override
    public void addShape(ImageDrawCall drawCall) {
        // determine sampler ID
        int samplerID = -1;
        for (int i = 0; i < NUM_TEXTURE_UNITS; i++) {
            if (textureIDs[i] == drawCall.textureID) {
                // use existing entry
                samplerID = i;
                break;
            }
            if (textureIDs[i] == NO_TEXTURE) {
                // create new entry
                textureIDs[i] = drawCall.textureID;
                samplerID = i;
                break;
            }
        }

        if (samplerID != -1)
            drawCall.samplerID = samplerID;
        else
            throw new RuntimeException("Maximum number of image textures used. Unable to draw image.");

        super.addShape(drawCall);
    }

    @Override
    public void render(Matrix3f viewMatrix) {
        for (int i = 0; i < textureIDs.length; i++) {
            int textureID = textureIDs[i];
            if (textureID != NO_TEXTURE) {
                GL13.glActiveTexture(GL13.GL_TEXTURE0 + i);
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);

                // GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER,
                // GL11.GL_NEAREST);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            }
        }

        super.render(viewMatrix);

        clearTextureIDs();
    }

    private void clearTextureIDs() {
        for (int i = 0; i < textureIDs.length; i++) {
            textureIDs[i] = NO_TEXTURE;
        }
    }

    @Override
    protected void loadVBOs(ArrayList<Batch> batches, int vertexCount) {
        IntVBO dataIndex = model.getIntVBO("dataIndex");
        MatrixVBO transformationMatrix = model.getMatrixVBO("transformationMatrix");
        FloatVBO position = model.getFloatVBO("position");
        FloatVBO depth = model.getFloatVBO("depth");
        FloatVBO size = model.getFloatVBO("size");

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
    }
}