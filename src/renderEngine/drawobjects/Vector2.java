package renderEngine.drawobjects;

import org.lwjglx.util.vector.Vector2f;

public class Vector2 implements AttributeValue {

    private Vector2f vector;

    public Vector2(float x, float y) {
        vector = new Vector2f(x, y);
    }

    @Override
    public void putData(float[] array, int index) {
        array[index] = vector.x;
        array[index + 1] = vector.y;
    }
}