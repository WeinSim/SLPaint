package renderEngine.drawcalls;

public abstract class DrawCall {

    private int dataIndex;

    public int getDataIndex() {
        return dataIndex;
    }

    public void setDataIndex(int dataIndex) {
        this.dataIndex = dataIndex;
    }
}