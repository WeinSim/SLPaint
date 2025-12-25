package renderEngine.shaders.drawcalls;

import renderEngine.shaders.bufferobjects.UBOEntry;

public abstract class DrawCall {

    private int dataIndex;

    public DrawCall() {
    }

    public abstract UBOEntry getUBOEntry();

    public int getDataIndex() {
        return dataIndex;
    }

    public void setDataIndex(int dataIndex) {
        this.dataIndex = dataIndex;
    }
}