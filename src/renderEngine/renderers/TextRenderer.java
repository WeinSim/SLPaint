package renderEngine.renderers;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import renderEngine.Loader;
import renderEngine.bufferobjects.VBOData;
import renderEngine.bufferobjects.VBOFloatData;
import renderEngine.bufferobjects.VBOIntData;
import renderEngine.drawcalls.TextDrawCall;
import renderEngine.fonts.TextFont;
import sutil.math.SVector;

public class TextRenderer extends GeometryShapeRenderer<TextDrawCall> {

    public TextRenderer(Loader loader) {
        super("text", loader);

        this.loader = loader;
    }

    @Override
    protected VBOData[] getVBOs(ArrayList<Batch> batches, int vertexCount) {
        VBOIntData dataIndex = new VBOIntData(vertexCount, 1);
        VBOFloatData position = new VBOFloatData(vertexCount, 2);
        VBOFloatData depth = new VBOFloatData(vertexCount, 1);
        VBOIntData charIndex = new VBOIntData(vertexCount, 1);

        int batchIndex = 0;
        for (Batch batch : batches) {
            for (TextDrawCall drawCall : batch.getDrawCalls()) {
                drawCall.font.putCharsIntoVBOs(drawCall, dataIndex, position, depth, charIndex, batchIndex);
            }
            batchIndex++;
        }

        return new VBOData[] { dataIndex, position, depth, charIndex };
    }

    @Override
    protected int getNumVertices(Batch batch) {
        int numVertices = 0;
        for (TextDrawCall drawCall : batch.getDrawCalls()) {
            numVertices += drawCall.text.length();
        }
        return numVertices;
    }

    @Override
    public void prepare() {
        TextFont font = lastRemovedBatch.getDrawCalls().get(0).font;

        // texture
        int[] textureIDs = font.getTextureIDs();

        final int[] textureUnits = {
                GL13.GL_TEXTURE0,
                GL13.GL_TEXTURE1,
                GL13.GL_TEXTURE2,
                GL13.GL_TEXTURE3
        };

        for (int i = 0; i < textureIDs.length; i++) {
            GL13.glActiveTexture(textureUnits[i]);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureIDs[i]);

            String name = String.format("textureSamplers[%d]", i);
            shaderProgram.loadUniform(name, i);
        }

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        // font data
        shaderProgram.loadUBOData("FontData", font.getUBOData());

        // uniform variables
        shaderProgram.loadUniform("textureSize", new SVector(font.getTextureWidth(), font.getTextureHeight()));
    }
}