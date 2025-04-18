package renderEngine.drawcalls;

public class RectFillVAO extends DrawVAO<RectFillDrawCall, RectFillData> {

    public RectFillVAO() {
        super();
    }

    @Override
    protected int getVertexCount(RectFillDrawCall drawCall) {
        return 1;
    }
}