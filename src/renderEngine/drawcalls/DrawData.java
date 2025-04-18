package renderEngine.drawcalls;

public interface DrawData {

    /**
     * This method must return a {@code float[]} whose length is exactly
     * {@code 4 * getNumUBOArrays()}.
     */
    public float[] getUBOData();

    public int getNumUBOArrays();
}