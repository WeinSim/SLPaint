package renderEngine.drawcalls;

import java.util.ArrayList;

public abstract class DrawCallList<C extends DrawCall, D extends DrawData, V extends DrawVAO<C, D>> {

    private ArrayList<V> vaos;

    public DrawCallList() {
        vaos = new ArrayList<>();
    }

    public void addDrawCall(C call, D data) {
        // Determine a TextVAO that already contains the correct text data.
        V vao = null;
        int dataIndex = -1;
        for (V v : vaos) {
            dataIndex = v.getDataIndex(data);
            if (dataIndex != -1) {
                vao = v;
                break;
            }
        }
        if (vao == null) {
            // No VAO has the correct text data. Create new one if neccessary.
            boolean createNewVAO = true;
            if (!vaos.isEmpty()) {
                vao = vaos.getLast();
                createNewVAO = !vao.hasRemainingCapacity();
            }
            if (createNewVAO) {
                vao = createNewVAO();
                vaos.add(vao);
            }
        }
        if (dataIndex == -1) {
            // The text data already exists in some VAO.
            vao.addDrawCall(call, data);
        } else {
            // A new array index needs to be allocated for the text data.
            vao.addDrawCall(call, dataIndex);
        }
    }

    protected abstract V createNewVAO();

    public ArrayList<V> getVAOs() {
        return vaos;
    }
}