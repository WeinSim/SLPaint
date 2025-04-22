package renderEngine.drawobjects;

import java.nio.Buffer;
import java.nio.FloatBuffer;

public final record Attribute<T extends AttributeValue, B extends Buffer>(int coordinateSize) {

    public static final Attribute<Vector3, FloatBuffer> POSITION = new Attribute<>(2);
    public static final Attribute<Vector2, FloatBuffer> SIZE = new Attribute<>(3);
    public static final Attribute<Vector4, FloatBuffer> CLIP_AREA = new Attribute<>(4);

}