package renderEngine.renderers;

import java.util.ArrayList;

import org.lwjglx.util.vector.Matrix3f;

import renderEngine.Loader;
import renderEngine.bufferobjects.UBOEntry;
import renderEngine.bufferobjects.VBOData;
import renderEngine.drawcalls.DrawCall;
import renderEngine.shaders.ShaderProgram;
import renderEngine.shaders.ShaderType;

public abstract class ShapeRenderer<C extends DrawCall> {

    // protected final ShaderType shaderType;
    protected final ShaderProgram shaderProgram;

    protected ArrayList<Batch> batches;

    /**
     * Used (in a bit of an ugly way) to let the TextRenderer know the font that is
     * being worked with.
     * 
     * This approach will not work for multiple fonts, but this is not the only
     * place in the code where introducing multiple fonts would cause a problem.
     */
    protected Batch lastRemovedBatch;

    protected Loader loader;

    public ShapeRenderer(String shaderName, ShaderType shaderType, Loader loader) {
        // this.shaderType = shaderType;
        this.loader = loader;

        shaderProgram = new ShaderProgram(shaderName, shaderType, loader);

        batches = new ArrayList<>();
    }

    public void addShape(C drawCall) {
        UBOEntry newUBO = drawCall.getGroupAttributes();
        Batch existingBatch = getExistingBatch(newUBO);
        if (existingBatch == null) {
            existingBatch = new Batch(newUBO);
            batches.add(existingBatch);
        }
        existingBatch.addDrawCall(drawCall);
    }

    protected Batch getExistingBatch(UBOEntry uboEntry) {
        for (Batch batch : batches) {
            if (batch.getGroupAttributes().equals(uboEntry))
                return batch;
        }
        return null;
    }

    public abstract void render(Matrix3f viewMatrix);

    protected abstract VBOData[] getVBOs(ArrayList<Batch> batches, int vertexCount);

    /**
     * This method is used e.g. by the text shader to load the texture data and font
     * data to the shader.
     */
    public void prepare() {

    }

    /**
     * This method is used e.g. by the image shader to clear its list of textureIDs.
     */
    public void finish() {

    }

    protected int getNumVertices(Batch batch) {
        return batch.getDrawCalls().size();
    }

    /**
     * Represents a list of draw calls that share a UBO entry.
     */
    protected class Batch {

        private UBOEntry groupAttributes;
        private ArrayList<C> drawCalls;

        public Batch(UBOEntry groupAttributes) {
            this.groupAttributes = groupAttributes;
            drawCalls = new ArrayList<>();
        }

        public void addDrawCall(C drawCall) {
            drawCalls.add(drawCall);
        }

        public UBOEntry getGroupAttributes() {
            return groupAttributes;
        }

        public ArrayList<C> getDrawCalls() {
            return drawCalls;
        }
    }
}