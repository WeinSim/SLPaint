package renderEngine.drawcalls;

import org.lwjglx.util.vector.Matrix3f;

public class TextDrawCall extends DrawCall {

    private final String text;
    private final Matrix3f transformationMatrix;
    private final double depth;

    public TextDrawCall(String text, Matrix3f transformationMatrix, double depth) {
        super();

        this.text = text;
        this.transformationMatrix = transformationMatrix;
        this.depth = depth;
    }

    public String getText() {
        return text;
    }

    public Matrix3f getTransformationMatrix() {
        return transformationMatrix;
    }

    public double getDepth() {
        return depth;
    }
}