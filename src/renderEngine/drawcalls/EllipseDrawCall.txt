package renderEngine.drawcalls;

import sutil.math.SVector;

public class EllipseDrawCall extends DrawCall {

    private final SVector position;
    private final SVector size;
    private final double depth;
    private final SVector color;
    private final double alpha;

    public EllipseDrawCall(SVector position, SVector size, double depth, SVector color, double alpha) {
        this.position = position;
        this.size = size;
        this.depth = depth;
        this.color = color;
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

    public SVector getColor() {
        return color;
    }

    public double getAlpha() {
        return alpha;
    }
}