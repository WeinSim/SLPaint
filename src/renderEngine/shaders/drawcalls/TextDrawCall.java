package renderEngine.shaders.drawcalls;

import org.lwjglx.util.vector.Matrix3f;

import renderEngine.ClipAreaInfo;
import renderEngine.fonts.TextFont;
import renderEngine.shaders.bufferobjects.UBOEntry;
import sutil.math.SVector;

public final class TextDrawCall extends DrawCall {

    public final SVector position;
    public final double depth;
    public final double relativeSize;
    public final Matrix3f uiMatrix;
    public final ClipAreaInfo clipAreaInfo;
    public final SVector color;
    public final double alpha;

    public final String text;
    public final TextFont font;

    public TextDrawCall(SVector position, double depth, double relativeSize, Matrix3f uiMatrix,
            ClipAreaInfo clipAreaInfo, SVector color, double alpha, String text, TextFont font) {

        this.position = position;
        this.relativeSize = relativeSize;
        this.depth = depth;
        this.uiMatrix = uiMatrix;
        this.clipAreaInfo = clipAreaInfo;
        this.color = color;
        this.alpha = alpha;
        this.text = text;
        this.font = font;
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
                (float) color.x,
                (float) color.y,
                (float) color.z,
                (float) alpha,
                uiMatrix.m00,
                uiMatrix.m01,
                uiMatrix.m02,
                (float) relativeSize,
                uiMatrix.m10,
                uiMatrix.m11,
                uiMatrix.m12,
                0.0f,
                uiMatrix.m20,
                uiMatrix.m21,
                uiMatrix.m22,
                0.0f
        });
    }
}