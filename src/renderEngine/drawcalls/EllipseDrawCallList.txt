package renderEngine.drawcalls;

public class EllipseDrawCallList extends DrawCallList<EllipseDrawCall, ClipAreaInfo> {

    public EllipseDrawCallList() {
        super(_ -> 1);
    }
}