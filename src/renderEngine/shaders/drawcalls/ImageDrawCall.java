package renderEngine.shaders.drawcalls;

import org.lwjglx.util.vector.Matrix3f;

import renderEngine.ClipAreaInfo;
import renderEngine.shaders.bufferobjects.UBOEntry;
import sutil.math.SVector;

public final class ImageDrawCall extends DrawCall {

    public final SVector position;
    public final double depth;
    public final SVector size;
    public final Matrix3f uiMatrix;
    public final ClipAreaInfo clipAreaInfo;
    public final int textureID;
    public int samplerID;

    public ImageDrawCall(SVector position, double depth, SVector size, Matrix3f uiMatrix, ClipAreaInfo clipAreaInfo,
            int textureID) {

        this.position = position;
        this.depth = depth;
        this.size = size;
        this.uiMatrix = uiMatrix;
        this.clipAreaInfo = clipAreaInfo;
        this.textureID = textureID;
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
                samplerID,
                0.0f,
                0.0f,
                0.0f
        });
    }
}