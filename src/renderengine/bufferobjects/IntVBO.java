package renderengine.bufferobjects;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL33;

public final class IntVBO extends AttributeVBO {

    public IntVBO(String attribuetName, int attributeNumber, int coordinateSize, VBOType type) {
        if (coordinateSize != 1) {
            throw new IllegalArgumentException(String.format(
                    """
                            Illegal coordinate size for integer VBO (%d).
                            Currently, only a coordinate size of 1 is supported.
                                                """,
                    coordinateSize));
        }

        super(attribuetName, attributeNumber, coordinateSize, type);
    }

    @Override
    public void init() {
        bind();
        GL30.glVertexAttribIPointer(attributeNumber, coordinateSize, GL11.GL_INT, 0, 0);
        GL33.glVertexAttribDivisor(attributeNumber,
                switch (type) {
                    case VERTEX -> 0;
                    case INSTANCE -> 1;
                });
        unbind();
    }
}