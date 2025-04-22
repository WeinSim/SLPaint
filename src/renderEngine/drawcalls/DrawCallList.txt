package renderEngine.drawcalls;

import java.util.ArrayList;
import java.util.function.Function;

public class DrawCallList<C extends DrawCall, D extends DrawData> {

    private ArrayList<DrawVAO<C, D>> vaos;

    private final Function<C, Integer> vertexCountFunction;

    public DrawCallList(Function<C, Integer> vertexCountFunction) {
        this.vertexCountFunction = vertexCountFunction;

        vaos = new ArrayList<>();
    }

    public void addDrawCall(C call, D data) {
        // Determine a DrawVAO that already contains the correct draw data.
        DrawVAO<C, D> vao = null;
        int dataIndex = -1;
        for (DrawVAO<C, D> v : vaos) {
            dataIndex = v.getDataIndex(data);
            if (dataIndex != -1) {
                vao = v;
                break;
            }
        }
        if (vao == null) {
            // No VAO has the correct draw data. Create new one if neccessary.
            boolean createNewVAO = true;
            if (!vaos.isEmpty()) {
                vao = vaos.getLast();
                createNewVAO = !vao.hasRemainingCapacity();
            }
            if (createNewVAO) {
                vao = new DrawVAO<C, D>(vertexCountFunction);
                vaos.add(vao);
            }
        }
        if (dataIndex == -1) {
            // The draw data already exists in some VAO.
            vao.addDrawCall(call, data);
        } else {
            // A new array index needs to be allocated for the draw data.
            vao.addDrawCall(call, dataIndex);
        }
    }

    public ArrayList<DrawVAO<C, D>> getVAOs() {
        return vaos;
    }
}