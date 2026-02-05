package renderengine.bufferobjects;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL33;

public final class FloatVBO extends AttributeVBO {

    public FloatVBO(String attribuetName, int attributeNumber, int coordinateSize, VBOType type) {
        super(attribuetName, attributeNumber, coordinateSize, type);
    }

    @Override
    public void init() {
        bind();
        GL20.glVertexAttribPointer(attributeNumber, coordinateSize, GL11.GL_FLOAT, false, 0, 0);
        GL33.glVertexAttribDivisor(attributeNumber,
                switch (type) {
                    case VERTEX -> 0;
                    case INSTANCE -> 1;
                });
        unbind();
    }
}