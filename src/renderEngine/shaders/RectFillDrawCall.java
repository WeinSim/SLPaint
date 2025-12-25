package renderEngine.shaders;

import org.lwjglx.util.vector.Matrix3f;

import renderEngine.ClipAreaInfo;
import sutil.math.SVector;

public class RectFillDrawCall extends DrawCall {

    public final SVector position;
    public final double depth;
    public final SVector size;
    public final Matrix3f uiMatrix;
    public final ClipAreaInfo clipAreaInfo;
    public final SVector color1;
    public final double alpha;
    public final SVector color2;
    public final double checkerboardSize;
    public final boolean applyCheckerboard;

    public RectFillDrawCall(SVector position, double depth, SVector size, Matrix3f uiMatrix, ClipAreaInfo clipAreaInfo,
            SVector color1, double alpha, SVector color2, double checkerboardSize, boolean applyCheckerboard) {

        this.position = position;
        this.depth = depth;
        this.size = size;
        this.uiMatrix = uiMatrix;
        this.clipAreaInfo = clipAreaInfo;
        this.color1 = color1;
        this.alpha = alpha;
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
                (float) color2.x,
                (float) color2.y,
                (float) color2.z,
                (float) alpha, // TODO: alpha for color2
                applyCheckerboard ? (float) checkerboardSize : -1,
                0.0f,
                0.0f,
                0.0f
        });
    }
}