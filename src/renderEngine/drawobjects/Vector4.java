package renderEngine.drawobjects;

import org.lwjglx.util.vector.Vector4f;

public class Vector4 implements AttributeValue {

    private Vector4f vector;

    public Vector4(float x, float y, float z, float w) {
        vector = new Vector4f(x, y, z, w);
    }

    @Override
    public void putData(float[] array, int index) {
        array[index] = vector.x;
        array[index + 1] = vector.y;
        array[index + 2] = vector.z;
        array[index + 3] = vector.w;
    }
}