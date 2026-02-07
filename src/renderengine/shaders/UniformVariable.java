package renderengine.shaders;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;
import org.lwjglx.util.vector.Matrix2f;
import org.lwjglx.util.vector.Matrix3f;
import org.lwjglx.util.vector.Matrix4f;
import org.lwjglx.util.vector.Vector4f;

import sutil.math.SVector;

public class UniformVariable {

    private static FloatBuffer matrixBuffer16 = BufferUtils.createFloatBuffer(16);
    private static FloatBuffer matrixBuffer9 = BufferUtils.createFloatBuffer(9);
    private static FloatBuffer matrixBuffer4 = BufferUtils.createFloatBuffer(4);

    private Datatype type;
    private String name;
    private int location;

    public UniformVariable(Datatype type, String name) {
        this.type = type;
        this.name = name;
    }

    public void load(Object value) {
        switch (type) {
            case INT, SAMPLER_2D -> GL20.glUniform1i(location, (int) value);
            case FLOAT ->
                GL20.glUniform1f(location,
                        value instanceof Double d
                                ? d.floatValue()
                                : (value instanceof Float f ? f : 0));
            case VEC2 -> {
                SVector v = (SVector) value;
                GL20.glUniform2f(location, (float) v.x, (float) v.y);
            }
            case VEC3 -> {
                SVector v = (SVector) value;
                GL20.glUniform3f(location, (float) v.x, (float) v.y, (float) v.z);
            }
            case VEC4 -> {
                Vector4f v = (Vector4f) value;
                GL20.glUniform4f(location,  v.x,  v.y,  v.z, v.w);
            }
            case MAT2 -> {
                Matrix2f matrix = (Matrix2f) value;
                matrix.store(matrixBuffer4);
                matrixBuffer4.flip();
                GL20.glUniformMatrix2fv(location, false, matrixBuffer4);
            }
            case MAT3 -> {
                Matrix3f matrix = (Matrix3f) value;
                matrix.store(matrixBuffer9);
                matrixBuffer9.flip();
                GL20.glUniformMatrix3fv(location, false, matrixBuffer9);
            }
            case MAT4 -> {
                Matrix4f matrix = (Matrix4f) value;
                matrix.store(matrixBuffer16);
                matrixBuffer16.flip();
                GL20.glUniformMatrix4fv(location, false, matrixBuffer16);
            }
        }
    }

    public void setLocation(int location) {
        this.location = location;
    }

    public String getName() {
        return name;
    }
}