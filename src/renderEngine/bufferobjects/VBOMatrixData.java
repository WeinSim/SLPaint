package renderEngine.bufferobjects;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjglx.util.vector.Matrix2f;
import org.lwjglx.util.vector.Matrix3f;
import org.lwjglx.util.vector.Matrix4f;

public final class VBOMatrixData extends VBOData {

    private FloatBuffer buffer;

    /**
     * Use {@code coordinateSize = n} for an nxn matrix.
     */
    public VBOMatrixData(int vertexCount, int coordinateSize) {
        super(coordinateSize);

        buffer = BufferUtils.createFloatBuffer(vertexCount * coordinateSize * coordinateSize);
    }

    @Override
    public void putData(Object data) {
        String simpleName = data.getClass().getSimpleName();
        switch (coordinateSize) {
            case 2 -> {
                if (data instanceof Matrix2f m) {
                    m.store(buffer);
                } else {
                    badVBODatatype("Matrix2f", simpleName);
                }
            }
            case 3 -> {
                if (data instanceof Matrix3f m) {
                    m.store(buffer);
                } else {
                    badVBODatatype("Matrix3f", simpleName);
                }
            }
            case 4 -> {
                if (data instanceof Matrix4f m) {
                    m.store(buffer);
                } else {
                    badVBODatatype("Matrix4f", simpleName);
                }
            }
        }
    }

    @Override
    public int storeDataInAttributeList(int attributeNumber) {
        buffer.flip();

        int vboID = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
        int stride = coordinateSize * coordinateSize * Float.BYTES;
        for (int i = 0; i < coordinateSize; i++) {
            int offset = coordinateSize * i * Float.BYTES;
            GL20.glVertexAttribPointer(attributeNumber + i, coordinateSize, GL11.GL_FLOAT, false, stride, offset);
        }
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        return vboID;
    }

    @Override
    public int getNumAttributes() {
        return coordinateSize;
    }
}