package renderEngine.renderers;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjglx.util.vector.Matrix3f;

import renderEngine.Loader;
import renderEngine.RawModel;
import renderEngine.bufferobjects.UniformBufferObject;
import renderEngine.bufferobjects.VBOData;
import renderEngine.drawcalls.DrawCall;
import renderEngine.shaders.ShaderType;

public abstract class GeometryShapeRenderer<C extends DrawCall> extends ShapeRenderer<C> {

    public GeometryShapeRenderer(String name, Loader loader) {
        super(name, ShaderType.GEOMETRY, loader);
    }

    @Override
    public void render(Matrix3f viewMatrix) {
        shaderProgram.start();
        shaderProgram.loadUniform("viewMatrix", viewMatrix);

        RawModel model;
        while ((model = getNextRawModel(loader)) != null) {
            GL30.glBindVertexArray(model.vaoID());

            for (int i = 0; i < model.numAttributes(); i++)
                GL20.glEnableVertexAttribArray(i);

            prepare();

            GL11.glDrawArrays(GL11.GL_POINTS, 0, model.vertexCount());

            for (int i = 0; i < model.numAttributes(); i++)
                GL20.glDisableVertexAttribArray(i);
        }

        finish();
        shaderProgram.stop();
    }

    private RawModel getNextRawModel(Loader loader) {
        if (batches.isEmpty())
            return null;

        int numBatches = Math.min(batches.size(), UniformBufferObject.UBO_ARRAY_LENGTH);
        int startIndex = batches.size() - numBatches;

        ArrayList<Batch> nextBatches = new ArrayList<>();
        int vertexCount = 0;

        int uboSize = batches.get(startIndex).getGroupAttributes().size() * numBatches;
        ByteBuffer uboBuffer = BufferUtils.createByteBuffer(uboSize);
        for (int i = 0; i < numBatches; i++) {
            Batch batch = batches.removeLast();
            uboBuffer.put(batch.getGroupAttributes().getBuffer());
            nextBatches.add(batch);
            lastRemovedBatch = batch;
            vertexCount += getNumVertices(batch);
        }

        shaderProgram.loadUBOData("GroupAttributes", uboBuffer);

        VBOData[] vbos = getVBOs(nextBatches, vertexCount);
        return loader.loadToVAO(vbos, vertexCount);
    }
}