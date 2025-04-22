package renderEngine.drawobjects;

import org.lwjglx.util.vector.Matrix3f;

import renderEngine.ClipAreaInfo;
import renderEngine.drawobjects.RectFill.RectDrawCall;
import sutil.math.SVector;

public class RectFill extends Renderable<RectDrawCall> {

    public RectFill() {
        super(
                new Attribute[] { Attribute.POSITION, Attribute.SIZE },
                new Attribute[] { Attribute.CLIP_AREA },
                "RectData",
                "rectFill");
    }

    public static class RectDrawCall extends DrawCall {

        public RectDrawCall(SVector position, double depth, SVector size, Matrix3f uiMatrix, ClipAreaInfo clipAreaInfo,
                SVector color1, double alpha, SVector color2, double checkerboardSize, boolean applyCheckerboard) {

            super(position, depth, size, uiMatrix, clipAreaInfo, color1, alpha, color2, checkerboardSize,
                    applyCheckerboard);
        }
    }
}