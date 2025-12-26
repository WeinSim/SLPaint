package renderEngine.shaders.drawcalls;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;

import renderEngine.Loader;
import renderEngine.RawModel;
import renderEngine.shaders.ShaderProgram;
import renderEngine.shaders.bufferobjects.UBOEntry;
import renderEngine.shaders.bufferobjects.UniformBufferObject;
import renderEngine.shaders.bufferobjects.VBOData;

public abstract class ShapeCollector<C extends DrawCall> {

    private final int numUBOArrays;
    private final String uboName;
    protected final ShaderProgram shaderProgram;

    private ArrayList<Batch> batches;

    /**
     * Used (in a bit of an ugly way) to let the TextCollector know the font that is
     * being worked with.
     * 
     * This approach will not work for multiple fonts, but this is not the only
     * place in the code where introducing multiple fonts would cause a problem.
     */
    protected Batch lastRemovedBatch;

    public ShapeCollector(int numUBOArrays, String uboName, String shaderName, String[] attributeNames,
            int[] attributeSizes) {

        this.numUBOArrays = numUBOArrays;
        this.uboName = uboName;

        shaderProgram = new ShaderProgram(shaderName, attributeNames, attributeSizes, true);

        batches = new ArrayList<>();
    }

    public void addShape(C drawCall) {
        UBOEntry newUBO = drawCall.getUBOEntry();
        Batch existingBatch = getExistingBatch(newUBO);
        if (existingBatch == null) {
            existingBatch = new Batch(newUBO);
            batches.add(existingBatch);
        }
        existingBatch.addDrawCall(drawCall);
    }

    private Batch getExistingBatch(UBOEntry uboEntry) {
        for (Batch batch : batches) {
            if (batch.getUBOEntry().equals(uboEntry))
                return batch;
        }
        return null;
    }

    public RawModel getNextRawModel(Loader loader) {
        if (batches.isEmpty())
            return null;

        int numBatches = Math.min(batches.size(), UniformBufferObject.UBO_ARRAY_LENGTH);
        int startIndex = batches.size() - numBatches;

        // UBOs
        float[] array = new float[numUBOArrays * 4 * UniformBufferObject.UBO_ARRAY_LENGTH];
        for (int i = 0; i < numBatches; i++) {
            // putting them into the array in reverse order because the batches are removed
            // from the ArrayList in reverse order
            batches.get(startIndex + i).getUBOEntry().putData(array, numBatches - 1 - i);
        }
        FloatBuffer uboBuffer = BufferUtils.createFloatBuffer(array.length);
        uboBuffer.put(array);
        shaderProgram.setUniformBlockData(uboName, uboBuffer);
        shaderProgram.syncUniformBlock(uboName, loader);

        // VBOs
        ArrayList<Batch> nextBatches = new ArrayList<>();
        int vertexCount = 0;
        for (int i = 0; i < numBatches; i++) {
            Batch batch = batches.removeLast();
            nextBatches.add(batch);
            lastRemovedBatch = batch;
            vertexCount += getNumVertices(batch);
        }
        VBOData[] vbos = getVBOs(nextBatches, vertexCount);

        return loader.loadToVAO(vbos, vertexCount);
    }

    protected abstract VBOData[] getVBOs(ArrayList<Batch> batches, int vertexCount);

    protected int getNumVertices(Batch batch) {
        return batch.getDrawCalls().size();
    }

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

    public ShaderProgram getShaderProgram() {
        return shaderProgram;
    }

    protected class Batch {

        private UBOEntry uboEntry;
        private ArrayList<C> drawCalls;

        public Batch(UBOEntry uboEntry) {
            this.uboEntry = uboEntry;
            drawCalls = new ArrayList<>();
        }

        public void addDrawCall(C drawCall) {
            drawCalls.add(drawCall);
        }

        public UBOEntry getUBOEntry() {
            return uboEntry;
        }

        public ArrayList<C> getDrawCalls() {
            return drawCalls;
        }
    }
}