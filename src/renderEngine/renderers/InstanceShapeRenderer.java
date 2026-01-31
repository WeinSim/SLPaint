package renderEngine.renderers;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.lwjglx.util.vector.Matrix3f;
import org.lwjglx.util.vector.Vector2f;

import renderEngine.Loader;
import renderEngine.RawModel;
import renderEngine.bufferobjects.UniformBufferObject;
import renderEngine.bufferobjects.VBOData;
import renderEngine.bufferobjects.VBOFloatData;
import renderEngine.drawcalls.DrawCall;
import renderEngine.shaders.ShaderType;

public abstract class InstanceShapeRenderer<C extends DrawCall> extends ShapeRenderer<C> {

    protected RawModel rawModel;

    public InstanceShapeRenderer(String name, Loader loader) {
        super(name, ShaderType.INSTANCE, loader);

        rawModel = createRawModel();
    }

    @Override
    public void render(Matrix3f viewMatrix) {
        shaderProgram.start();
        shaderProgram.loadUniform("viewMatrix", viewMatrix);

        GL30.glBindVertexArray(rawModel.vaoID());

        for (int i = 0; i < rawModel.numAttributes(); i++)
            GL20.glEnableVertexAttribArray(i);

        while (loadNextVBOData()) {
            prepare();

            int instanceCount = 1;
            int vertexCount = rawModel.vertexCount();
            GL31.glDrawElementsInstanced(GL11.GL_TRIANGLE_STRIP, vertexCount, GL11.GL_UNSIGNED_INT, 0, instanceCount);
            // GL11.glDrawElements(GL11.GL_TRIANGLE_STRIP, vertexCount,
            // GL11.GL_UNSIGNED_INT, 0);
        }

        for (int i = 0; i < rawModel.numAttributes(); i++)
            GL20.glDisableVertexAttribArray(i);

        finish();
        shaderProgram.stop();
    }

    private boolean loadNextVBOData() {
        if (batches.isEmpty())
            return false;

        int numBatches = Math.min(batches.size(), UniformBufferObject.UBO_ARRAY_LENGTH);
        int startIndex = batches.size() - numBatches;

        ArrayList<Batch> nextBatches = new ArrayList<>();
        int vertexCount = 0;

        int groupUBOSize = batches.get(startIndex).getGroupAttributes().size() * numBatches;
        ByteBuffer groupAttributes = BufferUtils.createByteBuffer(groupUBOSize);
        for (int i = 0; i < numBatches; i++) {
            Batch batch = batches.removeLast();
            groupAttributes.put(batch.getGroupAttributes().getBuffer());
            nextBatches.add(batch);
            lastRemovedBatch = batch;
            vertexCount += getNumVertices(batch);
        }

        shaderProgram.loadUBOData("GroupAttributes", groupAttributes);

        VBOData[] vbos = getVBOs(nextBatches, vertexCount);
        int attributeNumber = 0;
        for (VBOData vbo : vbos) {
            vbo.storeDataInAttributeList(attributeNumber);
            attributeNumber += vbo.getNumAttributes();
        }

        return true;
    }

    protected RawModel createRawModel() {
        VBOData[] vboData = new VBOData[1];
        vboData[0] = new VBOFloatData(4, 2);
        vboData[0].putData(new Vector2f(0f, 0f));
        vboData[0].putData(new Vector2f(1f, 0f));
        vboData[0].putData(new Vector2f(0f, 1f));
        vboData[0].putData(new Vector2f(1f, 1f));
        int[] indices = new int[] { 0, 1, 3, 3, 1, 2 };
        return loader.loadToVAO(vboData, indices);
    }
}