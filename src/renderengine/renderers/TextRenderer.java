package renderengine.renderers;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjglx.util.vector.Matrix3f;

import renderengine.Loader;
import renderengine.bufferobjects.FloatVBO;
import renderengine.bufferobjects.IntVBO;
import renderengine.drawcalls.TextDrawCall;
import renderengine.fonts.TextFont;
import sutil.math.SVector;

public class TextRenderer extends GeometryShapeRenderer<TextDrawCall> {

    private TextFont font;

    public TextRenderer(Loader loader) {
        super("text", loader);

        this.loader = loader;

        shaderProgram.start();
        for (int i = 0; i < 4; i++) {
            String name = String.format("textureSamplers[%d]", i);
            shaderProgram.loadUniform(name, i);
        }
        shaderProgram.stop();
    }

    @Override
    public void addShape(TextDrawCall drawCall) {
        font = drawCall.font;
        super.addShape(drawCall);
    }

    @Override
    public void render(Matrix3f viewMatrix) {
        int[] textureIDs = font.getTextureIDs();
        for (int i = 0; i < textureIDs.length; i++) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + i);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureIDs[i]);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        }

        // This looks a little wasteful, but it only takes ~3 microseconds
        // long startTime = System.nanoTime();
        float[] uboData = font.getUBOData();
        ByteBuffer uboBuffer = BufferUtils.createByteBuffer(uboData.length * Float.BYTES);
        for (float f : uboData)
            uboBuffer.putFloat(f);
        uboBuffer.flip();
        // long duration = System.nanoTime() - startTime;
        // System.out.format("Text data upload: %.3f µs\n", duration * 1e-3);

        shaderProgram.start();
        shaderProgram.loadUBOData("FontData", uboBuffer);
        shaderProgram.loadUniform("textureSize", new SVector(font.getTextureWidth(), font.getTextureHeight()));

        super.render(viewMatrix);
    }

    @Override
    protected void loadVBOs(ArrayList<Batch> batches, int vertexCount) {
        IntVBO dataIndex = model.getIntVBO("dataIndex");
        FloatVBO position = model.getFloatVBO("position");
        FloatVBO depth = model.getFloatVBO("depth");
        IntVBO charIndex = model.getIntVBO("charIndex");

        int batchIndex = 0;
        for (Batch batch : batches) {
            for (TextDrawCall drawCall : batch.getDrawCalls()) {
                drawCall.font.putCharsIntoVBOs(drawCall, dataIndex, position, depth, charIndex, batchIndex);
            }
            batchIndex++;
        }
    }

    @Override
    protected int getNumInstances(Batch batch) {
        int numVertices = 0;
        for (TextDrawCall drawCall : batch.getDrawCalls()) {
            numVertices += drawCall.text.length();
        }
        return numVertices;
    }
}