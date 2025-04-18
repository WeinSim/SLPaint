package renderEngine.drawcalls;

import sutil.math.SVector;

public class RectOutlineDrawCall extends DrawCall {

    private final SVector position, size;
    private final double depth;
    private final SVector color1;
    private final double strokeWeight;

    public RectOutlineDrawCall(SVector position, SVector size, double depth, SVector color1, double strokeWeight) {
        this.position = position;
        this.size = size;
        this.depth = depth;
        this.color1 = color1;
        this.strokeWeight = strokeWeight;
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

    public double getStrokeWeight() {
        return strokeWeight;
    }
}