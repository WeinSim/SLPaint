package renderEngine.drawcalls;

import sutil.math.SVector;

public class RectFillDrawCall extends DrawCall {

    private final SVector position, size;
    private final double depth;
    private final SVector color1;
    private final double alpha;

    public RectFillDrawCall(SVector position, SVector size, double depth, SVector color1, double alpha) {
        this.position = position;
        this.size = size;
        this.depth = depth;
        this.color1 = color1;
        this.alpha = alpha;
    }

    public SVector getPosition() {
        return position;
    }

    public SVector getSize() {
        return size;
    }

    public double getDepth() {
        return depth;
    }

    public SVector getColor1() {
        return color1;
    }

    public double getAlpha() {
        return alpha;
    }
}