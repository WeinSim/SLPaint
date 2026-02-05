package renderengine.drawcalls;

import renderengine.bufferobjects.UBOEntry;

public abstract class DrawCall {

    private int dataIndex;

    public DrawCall() {
    }

    public abstract UBOEntry getGroupAttributes();

    public int getDataIndex() {
        return dataIndex;
    }

    public void setDataIndex(int dataIndex) {
        this.dataIndex = dataIndex;
    }
}