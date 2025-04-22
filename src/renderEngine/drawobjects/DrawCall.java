package renderEngine.drawobjects;

import org.lwjglx.util.vector.Matrix3f;

import renderEngine.ClipAreaInfo;
import sutil.math.SVector;

public abstract class DrawCall {

    private AttributeMap attributeMap;

    public DrawCall(SVector position, double depth, SVector size, Matrix3f uiMatrix, ClipAreaInfo clipAreaInfo, SVector color1, double alpha,
            SVector color2, double checkerboardSize, boolean applyCheckerboard) {

        attributeMap = new AttributeMap();

        setAttribute(Attribute.POSITION, new Vector3((float) position.x, (float) position.y, (float) depth));
        // TODO: set other attributes
    }

    public <T extends AttributeValue> T getAttribute(Attribute<T, ?> attribute) {
        return attributeMap.get(attribute);
    }

    public <T extends AttributeValue> void setAttribute(Attribute<T, ?> attribute, T data) {
        attributeMap.set(attribute, data);
    }
}