package renderEngine.shaders;

public class UBOEntry {

    private float[] data;

    public UBOEntry(float[] data) {
        this.data = data;
    }

    /**
     * Two non-null {@code UBOData} objects are equal iff their {@code data} arrays
     * have the same length and their entries are pairwise equal.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;

        if (obj instanceof UBOEntry ubo) {
            if (data.length != ubo.data.length)
                return false;

            for (int i = 0; i < data.length; i++) {
                if (data[i] != ubo.data[i])
                    return false;
            }
            return true;
        }

        return false;
    }

    public void putData(float[] array, int index) {
        int numVec4Arrays = data.length / 4;
        for (int i = 0; i < numVec4Arrays; i++) {
            for (int j = 0; j < 4; j++) {
                int arrayIndex = 4 * (UniformBufferObject.UBO_ARRAY_LENGTH * i + index) + j;
                int dataIndex = 4 * i + j;
                array[arrayIndex] = data[dataIndex];
            }
        }
    }
}