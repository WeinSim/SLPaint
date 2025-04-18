package renderEngine.drawcalls;

public class RectFillDrawCallList extends DrawCallList<RectFillDrawCall, RectFillData, RectFillVAO> {

    public RectFillDrawCallList() {
        super();
    }

    @Override
    protected RectFillVAO createNewVAO() {
        return new RectFillVAO();
    }
}