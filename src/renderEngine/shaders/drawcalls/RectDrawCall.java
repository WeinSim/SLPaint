package renderEngine.shaders.drawcalls;

import org.lwjglx.util.vector.Matrix3f;
import org.lwjglx.util.vector.Vector4f;

import renderEngine.ClipAreaInfo;
import renderEngine.shaders.bufferobjects.UBOEntry;
import sutil.math.SVector;

public abstract sealed class RectDrawCall extends DrawCall permits RectFillDrawCall, RectOutlineDrawCall {

    public final SVector position;
    public final double depth;
    public final SVector size;
    public final Matrix3f uiMatrix;
    public final ClipAreaInfo clipAreaInfo;
    public final Vector4f color1;
    public final Vector4f color2;
    public final double checkerboardSize;
    public final boolean applyCheckerboard;

    public RectDrawCall(SVector position, double depth, SVector size, Matrix3f uiMatrix, ClipAreaInfo clipAreaInfo,
            Vector4f color1, Vector4f color2, double checkerboardSize, boolean applyCheckerboard) {

        this.position = position;
        this.depth = depth;
        this.size = size;
        this.uiMatrix = uiMatrix;
        this.clipAreaInfo = clipAreaInfo;
        this.color1 = color1;
        this.color2 = color2;
        this.checkerboardSize = checkerboardSize;
        this.applyCheckerboard = applyCheckerboard;
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
                color2.x,
                color2.y,
                color2.z,
                color2.w,
                applyCheckerboard ? (float) checkerboardSize : -1,
                0.0f,
                0.0f,
                0.0f
        });
    }
}