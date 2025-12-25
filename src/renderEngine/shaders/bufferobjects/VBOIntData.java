package renderEngine.shaders.bufferobjects;

import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;

public final class VBOIntData extends VBOData {

    private IntBuffer buffer;

    public VBOIntData(int vertexCount, int coordinateSize) {
        super(vertexCount, coordinateSize);

        if (coordinateSize != 1) {
            throw new IllegalArgumentException(String.format(
                    """
                            Illegal coordinate size for integer VBO (%d).
                            Currently, only a coordinate size of 1 is supported.
                                                """,
                    coordinateSize));
        }

        buffer = BufferUtils.createIntBuffer(vertexCount * coordinateSize);
    }

    public void putData(Object data) {
        if (data instanceof Integer i) {
            buffer.put(i);
        } else {
            System.err.format("Invalid data type for vbo attribute! Expected int, got %s\n", data.toString());
            System.exit(1);
        }
    }

    @Override
    public int storeDataInAttributeList(int attributeNumber) {
        buffer.flip();

        int vboID = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
        GL30.glVertexAttribIPointer(attributeNumber, coordinateSize, GL11.GL_INT, 0, 0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        return vboID;
    }

    @Override
    public int getNumAttributes() {
        return 1;
    }
}