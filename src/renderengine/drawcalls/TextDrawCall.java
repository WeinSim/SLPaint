package renderengine.drawcalls;

import org.lwjglx.util.vector.Matrix3f;
import org.lwjglx.util.vector.Vector4f;

import renderengine.bufferobjects.UBOEntry;
import renderengine.fonts.TextFont;
import sutil.math.SVector;

public final class TextDrawCall extends DrawCall {

    public final SVector position;
    public final double depth;
    public final double relativeSize;
    public final Matrix3f uiMatrix;
    public final ClipAreaInfo clipAreaInfo;
    public final Vector4f color;

    public final String text;
    public final TextFont font;

    public TextDrawCall(SVector position, double depth, double relativeSize, Matrix3f uiMatrix,
            ClipAreaInfo clipAreaInfo, Vector4f color, String text, TextFont font) {

        this.position = position;
        this.relativeSize = relativeSize;
        this.depth = depth;
        this.uiMatrix = uiMatrix;
        this.clipAreaInfo = clipAreaInfo;
        this.color = color;
        this.text = text;
        this.font = font;
    }

    @Override
    public UBOEntry getGroupAttributes() {
        boolean clip = clipAreaInfo.isEnabled();
        SVector boundingBoxPos = clipAreaInfo.getPosition();
        SVector boundingBoxSize = clipAreaInfo.getSize();
        UBOEntry ubo = new UBOEntry(24 * Float.BYTES);
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
        ubo.put(color);
        ubo.put(uiMatrix);
        ubo.put(relativeSize);
        ubo.putPadding(3 * Float.BYTES);
        return ubo.finish();
    }
}