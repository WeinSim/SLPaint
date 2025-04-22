package renderEngine.drawcalls;

import sutil.math.SVector;

public record RectData(ClipAreaInfo clipArea, boolean applyCheckerboard, SVector color2, double checkerboardSize)
        implements DrawData {

    @Override
    public float[] getUBOData() {
        float[] uboData = new float[8];
        clipArea.setUBOData(uboData);

        uboData[4] = (float) color2.x;
        uboData[5] = (float) color2.y;
        uboData[6] = (float) color2.z;
        uboData[7] = applyCheckerboard ? (float) checkerboardSize : -1;

        return uboData;
    }

    @Override
    public int getNumUBOArrays() {
        return 2;
    }
}