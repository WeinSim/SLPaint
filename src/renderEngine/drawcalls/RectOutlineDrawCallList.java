package renderEngine.drawcalls;

public class RectOutlineDrawCallList extends DrawCallList<RectOutlineDrawCall, RectData> {

    public RectOutlineDrawCallList() {
        super(_ -> 1);
    }
}