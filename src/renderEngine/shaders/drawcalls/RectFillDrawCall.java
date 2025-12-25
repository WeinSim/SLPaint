package renderEngine.shaders.drawcalls;

import org.lwjglx.util.vector.Matrix3f;

import renderEngine.ClipAreaInfo;
import sutil.math.SVector;

public final class RectFillDrawCall extends RectDrawCall {

    public RectFillDrawCall(SVector position, double depth, SVector size, Matrix3f uiMatrix, ClipAreaInfo clipAreaInfo,
            SVector color1, double alpha, SVector color2, double checkerboardSize, boolean applyCheckerboard) {

        super(position, depth, size, uiMatrix, clipAreaInfo, color1, alpha, color2, checkerboardSize,
                applyCheckerboard);
    }
}