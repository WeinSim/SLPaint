package renderengine.drawcalls;

import org.lwjglx.util.vector.Matrix3f;
import org.lwjglx.util.vector.Vector4f;

import renderengine.bufferobjects.UBOEntry;
import sutil.math.SVector;

public final class EllipseDrawCall extends DrawCall {

    public final SVector position;
    public final double depth;
    public final SVector size;
    public final Matrix3f uiMatrix;
    public final ClipAreaInfo clipAreaInfo;
    public final Vector4f color;

    public EllipseDrawCall(SVector position, double depth, SVector size, Matrix3f uiMatrix, ClipAreaInfo clipAreaInfo,
            Vector4f color) {

        this.position = position;
        this.depth = depth;
        this.size = size;
        this.uiMatrix = uiMatrix;
        this.clipAreaInfo = clipAreaInfo;
        this.color = color;
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