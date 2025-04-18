package renderEngine.drawcalls;

import sutil.math.SVector;

public record TextData(ClipAreaInfo clipArea, SVector color, double textScale) implements DrawData {

    @Override
    public float[] getUBOData() {
        float[] uboData = new float[8];
        clipArea.setUBOData(uboData);

        uboData[4] = (float) color.x;
        uboData[5] = (float) color.y;
        uboData[6] = (float) color.z;
        uboData[7] = (float) textScale;

        return uboData;
    }
}