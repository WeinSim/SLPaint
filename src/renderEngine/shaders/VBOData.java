package renderEngine.shaders;

public abstract sealed class VBOData permits VBOIntData, VBOFloatData, VBOMatrixData {

    protected final int vertexCount;
    protected final int coordinateSize;

    public VBOData(int vertexCount, int coordinateSize) {
        this.vertexCount = vertexCount;
        this.coordinateSize = coordinateSize;
    }

    public abstract void putData(Object data);

    public abstract int storeDataInAttributeList(int attributeNumber);

    public abstract int getNumAttributes();

    protected void badVBODatatype(String expected, String got) {
        final String exceptionBaseText = "Invalid data type for vbo attribute! Expected %s, got %s\n";
        throw new IllegalArgumentException(String.format(exceptionBaseText, expected, got));
    }

    public int vertexCount() {
        return vertexCount;
    }
}