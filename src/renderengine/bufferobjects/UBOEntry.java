package renderengine.bufferobjects;

import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;
import org.lwjglx.util.vector.Matrix2f;
import org.lwjglx.util.vector.Matrix3f;
import org.lwjglx.util.vector.Matrix4f;
import org.lwjglx.util.vector.Vector2f;
import org.lwjglx.util.vector.Vector3f;
import org.lwjglx.util.vector.Vector4f;

import sutil.math.SVector;

public class UBOEntry {

    private ByteBuffer buffer;

    /**
     * 
     * @param capacity Buffer capacity in bytes.
     */
    public UBOEntry(int size) {
        buffer = BufferUtils.createByteBuffer(size);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj == this)
            return true;
        if (obj instanceof UBOEntry e)
            return buffer.equals(e.buffer);

        return false;
    }

    public void put(Object data) {
        put(data, 0, buffer, true);
    }

    public void put(Object data, int coordinateSize) {
        put(data, coordinateSize, buffer, true);
    }

    public static void put(Object data, int coordinateSize, ByteBuffer buffer, boolean addStd140Padding) {
        // int remaining = buffer.remaining();
        // System.out.format("%s: %d remaining\n", this.toString(), remaining);
        String datatype = data.getClass().getSimpleName();
        // TODO: put switch(coordinateSize) as outer switch statement and check for
        // invalid coordinate size
        switch (data) {
            case Integer i -> buffer.putInt(i);
            case Float f -> buffer.putFloat(f);
            case Double d -> buffer.putFloat(d.floatValue());
            case SVector s -> {
                switch (coordinateSize) {
                    case 2 -> {
                        buffer.putFloat((float) s.x);
                        buffer.putFloat((float) s.y);
                    }
                    case 3 -> {
                        buffer.putFloat((float) s.x);
                        buffer.putFloat((float) s.y);
                        buffer.putFloat((float) s.z);
                    }
                    case 0 -> missingCoordinateSize(datatype);
                    default -> invalidCoordinateSize(datatype, coordinateSize);
                }
            }
            case Vector2f v -> {
                buffer.putFloat(v.x);
                buffer.putFloat(v.y);
            }
            case Vector3f v -> {
                buffer.putFloat(v.x);
                buffer.putFloat(v.y);
                buffer.putFloat(v.z);
            }
            case Vector4f v -> {
                buffer.putFloat(v.x);
                buffer.putFloat(v.y);
                buffer.putFloat(v.z);
                buffer.putFloat(v.w);
            }
            case Matrix2f m -> {
                buffer.putFloat(m.m00);
                buffer.putFloat(m.m01);
                buffer.putFloat(m.m10);
                buffer.putFloat(m.m11);
            }
            case Matrix3f m -> {
                buffer.putFloat(m.m00);
                buffer.putFloat(m.m01);
                buffer.putFloat(m.m02);
                if (addStd140Padding)
                    putPadding(Float.BYTES, buffer);
                buffer.putFloat(m.m10);
                buffer.putFloat(m.m11);
                buffer.putFloat(m.m12);
                if (addStd140Padding)
                    putPadding(Float.BYTES, buffer);
                buffer.putFloat(m.m20);
                buffer.putFloat(m.m21);
                buffer.putFloat(m.m22);
                if (addStd140Padding)
                    putPadding(Float.BYTES, buffer);
            }
            case Matrix4f m -> {
                buffer.putFloat(m.m00);
                buffer.putFloat(m.m01);
                buffer.putFloat(m.m02);
                buffer.putFloat(m.m03);
                buffer.putFloat(m.m10);
                buffer.putFloat(m.m11);
                buffer.putFloat(m.m12);
                buffer.putFloat(m.m13);
                buffer.putFloat(m.m20);
                buffer.putFloat(m.m21);
                buffer.putFloat(m.m22);
                buffer.putFloat(m.m23);
                buffer.putFloat(m.m30);
                buffer.putFloat(m.m31);
                buffer.putFloat(m.m32);
                buffer.putFloat(m.m33);
            }
            default -> unknownDatatype(datatype);
        }
    }

    public void putPadding(int numBytes) {
        putPadding(numBytes, buffer);
    }

    public static void putPadding(int numBytes, ByteBuffer buffer) {
        for (int i = 0; i < numBytes; i++) {
            buffer.put((byte) 0x00);
        }
    }

    private static void missingCoordinateSize(String datatype) {
        final String exceptionBaseText = "Missing coordinate size for datatype %s!";
        throw new IllegalArgumentException(String.format(exceptionBaseText, datatype));
    }

    private static void invalidCoordinateSize(String datatype, int coordinateSize) {
        final String exceptionBaseText = "Invalid coordinate size (%d) for datatype %s!";
        throw new IllegalArgumentException(String.format(exceptionBaseText, coordinateSize, datatype));
    }

    private static void unknownDatatype(String datatype) {
        final String exceptionBaseText = "Unknown datatype (%s) for BufferObject\n";
        throw new IllegalArgumentException(String.format(exceptionBaseText, datatype));
    }

    public UBOEntry finish() {
        if (buffer.hasRemaining())
            throw new RuntimeException("UBO is not filled!");

        buffer.flip();

        return this;
    }

    public int size() {
        return buffer.capacity();
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }
}