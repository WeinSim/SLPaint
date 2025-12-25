package renderEngine.shaders;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjglx.util.vector.Vector2f;
import org.lwjglx.util.vector.Vector3f;
import org.lwjglx.util.vector.Vector4f;

import sutil.math.SVector;

public final class VBOFloatData extends VBOData {

    private FloatBuffer buffer;

    public VBOFloatData(int vertexCount, int coordinateSize) {
        super(vertexCount, coordinateSize);

        buffer = BufferUtils.createFloatBuffer(vertexCount * coordinateSize);
    }

    @Override
    public void putData(Object data) {
        String simpleName = data.getClass().getSimpleName();
        switch (coordinateSize) {
            case 1 -> {
                switch (data) {
                    case Float f -> buffer.put(f);
                    case Double d -> buffer.put(d.floatValue());
                    default -> badVBODatatype("Float or Double", simpleName);
                }
            }
            case 2 -> {
                switch (data) {
                    case SVector v -> {
                        buffer.put((float) v.x);
                        buffer.put((float) v.y);
                    }
                    case Vector2f v -> {
                        buffer.put(v.x);
                        buffer.put(v.y);
                    }
                    default -> badVBODatatype("SVector or Vector2f", simpleName);
                }
            }
            case 3 -> {
                switch (data) {
                    case SVector v -> {
                        buffer.put((float) v.x);
                        buffer.put((float) v.y);
                        buffer.put((float) v.z);
                    }
                    case Vector3f v -> {
                        buffer.put(v.x);
                        buffer.put(v.y);
                        buffer.put(v.z);
                    }
                    default -> badVBODatatype("SVector or Vector3f", simpleName);
                }
            }
            case 4 -> {
                switch (data) {
                    case Vector4f v -> {
                        buffer.put(v.x);
                        buffer.put(v.y);
                        buffer.put(v.z);
                        buffer.put(v.w);
                    }
                    default -> badVBODatatype("Vector4f", simpleName);
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
        GL20.glVertexAttribPointer(attributeNumber, coordinateSize, GL11.GL_FLOAT, false, 0, 0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        return vboID;
    }

    @Override
    public int getNumAttributes() {
        return 1;
    }
}