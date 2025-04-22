package renderEngine.drawobjects;

import org.lwjglx.util.vector.Vector3f;

public class Vector3 implements AttributeValue {

    private Vector3f vector;

    public Vector3(float x, float y, float z) {
        vector = new Vector3f(x, y, z);
    }

    @Override
    public void putData(float[] array, int index) {
        array[index] = vector.x;
        array[index + 1] = vector.y;
        array[index + 2] = vector.z;
    }
}