package renderEngine.drawcalls;

public class RectFillDrawCallList extends DrawCallList<RectFillDrawCall, RectData> {

    public RectFillDrawCallList() {
        super(_ -> 1);
    }
}