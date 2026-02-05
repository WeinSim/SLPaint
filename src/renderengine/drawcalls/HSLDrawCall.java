package renderengine.drawcalls;

import org.lwjglx.util.vector.Matrix3f;

import renderengine.bufferobjects.UBOEntry;
import sutil.math.SVector;

public final class HSLDrawCall extends DrawCall {

    public static final int HUE_SAT_FIELD_CIRC = 0,
            HUE_SAT_FIELD_RECT = 1,
            LIGHTNESS_SCALE = 2,
            ALPHA_SCALE = 3;

    public static final int HSL = 0,
            HSV = 4;

    public static final int VERTICAL = 0,
            HORIZONTAL = 8;

    public final SVector position;
    public final double depth;
    public final SVector size;
    public final Matrix3f uiMatrix;
    public final ClipAreaInfo clipAreaInfo;

    public final SVector color;
    public final int flags;

    public HSLDrawCall(SVector position, double depth, SVector size, Matrix3f uiMatrix, ClipAreaInfo clipAreaInfo,
            SVector color, int flags) {

        this.position = position;
        this.depth = depth;
        this.size = size;
        this.uiMatrix = uiMatrix;
        this.clipAreaInfo = clipAreaInfo;

        this.color = color;
        this.flags = flags;
    }

    @Override
    public UBOEntry getGroupAttributes() {
        boolean clip = clipAreaInfo.isEnabled();
        SVector boundingBoxPos = clipAreaInfo.getPosition();
        SVector boundingBoxSize = clipAreaInfo.getSize();
        UBOEntry ubo = new UBOEntry(4 * Float.BYTES);
        if (clip) {
            ubo.put(boundingBoxPos.x);
            ubo.put(boundingBoxPos.y);
            ubo.put(boundingBoxPos.x + boundingBoxSize.x);
            ubo.put(boundingBoxPos.y + boundingBoxSize.y);
        } else {
            ubo.put(-Float.MAX_VALUE);
            ubo.put(-Float.MAX_VALUE);
            ubo.put(Float.MAX_VALUE);
            ubo.put(Float.MAX_VALUE);
        }
        return ubo.finish();
    }
}