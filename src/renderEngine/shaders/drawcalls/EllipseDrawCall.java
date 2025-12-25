package renderEngine.shaders.drawcalls;

import org.lwjglx.util.vector.Matrix3f;

import renderEngine.ClipAreaInfo;
import renderEngine.shaders.bufferobjects.UBOEntry;
import sutil.math.SVector;

public final class EllipseDrawCall extends DrawCall {

    public final SVector position;
    public final double depth;
    public final SVector size;
    public final Matrix3f uiMatrix;
    public final ClipAreaInfo clipAreaInfo;
    public final SVector color;
    public final double alpha;

    public EllipseDrawCall(SVector position, double depth, SVector size, Matrix3f uiMatrix, ClipAreaInfo clipAreaInfo,
            SVector color, double alpha) {

        this.position = position;
        this.depth = depth;
        this.size = size;
        this.uiMatrix = uiMatrix;
        this.clipAreaInfo = clipAreaInfo;
        this.color = color;
        this.alpha = alpha;
    }

    @Override
    public UBOEntry getUBOEntry() {
        boolean clip = clipAreaInfo.isEnabled();
        SVector boundingBoxPos = clipAreaInfo.getPosition();
        SVector boundingBoxSize = clipAreaInfo.getSize();
        return new UBOEntry(new float[] {
                clip ? (float) boundingBoxPos.x : -Float.MAX_VALUE,
                clip ? (float) boundingBoxPos.y : -Float.MAX_VALUE,
                clip ? (float) (boundingBoxPos.x + boundingBoxSize.x) : Float.MAX_VALUE,
                clip ? (float) (boundingBoxPos.y + boundingBoxSize.y) : Float.MAX_VALUE,
        });
    }
}