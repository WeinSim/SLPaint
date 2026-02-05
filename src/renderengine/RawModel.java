package renderengine;

import java.util.ArrayList;
import java.util.HashMap;

import org.lwjgl.opengl.GL30;

import renderengine.bufferobjects.AttributeVBO;
import renderengine.bufferobjects.FloatVBO;
import renderengine.bufferobjects.IndexVBO;
import renderengine.bufferobjects.IntVBO;
import renderengine.bufferobjects.MatrixVBO;
import renderengine.bufferobjects.VBO;
import renderengine.bufferobjects.VBOType;

public class RawModel {

    private final int vaoID;

    private final HashMap<String, AttributeVBO> vbos;
    private IndexVBO indexVBO;

    private int indexCount = -1;
    private int instanceCount = -1;

    public RawModel(Loader loader, ArrayList<AttributeVBO> vbos) {
        vaoID = GL30.glGenVertexArrays();
        bind();

        this.vbos = new HashMap<>();
        for (AttributeVBO vbo : vbos) {
            this.vbos.put(vbo.attributeName(), vbo);
            vbo.init();
        }

        unbind();

        indexVBO = null;
    }

    public void setIndices(int... indices) {
        bind();
        indexVBO = new IndexVBO(indices);
        indexCount = indexVBO.indexCount();
        unbind();
    }

    public void initInstanceVBOs(int instanceCount) {
        this.instanceCount = instanceCount;
        for (AttributeVBO vbo : vbos())
            if (vbo.type() == VBOType.INSTANCE)
                vbo.createBuffer(instanceCount);
    }

    public void finishInstanceVBOs() {
        for (AttributeVBO vbo : vbos())
            if (vbo.type() == VBOType.INSTANCE)
                vbo.storeDataInAttributeList();
    }

    public AttributeVBO getVBO(String name) {
        AttributeVBO vbo = vbos.get(name);
        if (vbo == null)
            vboNotFount(name);
        return vbo;
    }

    public FloatVBO getFloatVBO(String name) {
        AttributeVBO vbo = getVBO(name);
        if (vbo instanceof FloatVBO floatVBO)
            return floatVBO;
        invalidVBODatatype(name, "FloatVBO", vbo.getClass().getSimpleName());
        return null;
    }

    public MatrixVBO getMatrixVBO(String name) {
        AttributeVBO vbo = getVBO(name);
        if (vbo instanceof MatrixVBO matrixVBO)
            return matrixVBO;
        invalidVBODatatype(name, "MatrixVBO", vbo.getClass().getSimpleName());
        return null;
    }

    public IntVBO getIntVBO(String name) {
        AttributeVBO vbo = getVBO(name);
        if (vbo instanceof IntVBO intVBO)
            return intVBO;
        invalidVBODatatype(name, "IntVBO", vbo.getClass().getSimpleName());
        return null;
    }

    public void bind() {
        GL30.glBindVertexArray(vaoID);
    }

    public void unbind() {
        GL30.glBindVertexArray(0);
    }

    public void cleanUp() {
        for (VBO vbo : vbos())
            vbo.cleanUp();
        if (indexVBO != null)
            indexVBO.cleanUp();

        GL30.glDeleteVertexArrays(vaoID);
    }

    public int indexCount() {
        return indexCount;
    }

    public int instanceCount() {
        return instanceCount;
    }

    public void enableVBOs() {
        for (AttributeVBO vbo : vbos())
            vbo.enable();
    }

    public void disableVBOs() {
        for (AttributeVBO vbo : vbos())
            vbo.enable();
    }

    public int vaoID() {
        return vaoID;
    }

    private Iterable<AttributeVBO> vbos() {
        return vbos.values();
    }

    private static void vboNotFount(String name) {
        final String baseStr = "VBO \"%s\" does not exist";
        throw new RuntimeException(String.format(baseStr, name));
    }

    private static void invalidVBODatatype(String name, String expected, String got) {
        final String baseStr = "Unexpected datatype for VBO \"%s\". Expected %s, got %s.";
        throw new RuntimeException(String.format(baseStr, name, expected, got));
    }
}