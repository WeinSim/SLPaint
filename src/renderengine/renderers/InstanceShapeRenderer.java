package renderengine.renderers;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL31;
import org.lwjglx.util.vector.Matrix3f;
import org.lwjglx.util.vector.Vector2f;

import renderengine.Loader;
import renderengine.bufferobjects.FloatVBO;
import renderengine.drawcalls.DrawCall;
import renderengine.shaders.ShaderType;

public abstract class InstanceShapeRenderer<C extends DrawCall> extends ShapeRenderer<C> {

    public InstanceShapeRenderer(String name, Loader loader) {
        super(name, ShaderType.INSTANCE, loader);

        initRawModel();
    }

    @Override
    public void render(Matrix3f viewMatrix) {
        shaderProgram.start();
        shaderProgram.loadUniform("viewMatrix", viewMatrix);

        model.bind();
        model.enableVBOs();
        while (prepareNextDrawcall())
            GL31.glDrawArraysInstanced(GL11.GL_TRIANGLE_STRIP, 0, model.vertexCount(), model.instanceCount());
        model.disableVBOs();

        shaderProgram.stop();
    }

    protected void initRawModel() {
        initQuad();
    }

    protected void initQuad() {
        model.initVertexVBOs(4);

        FloatVBO cornerPos = model.getFloatVBO("cornerPos");
        cornerPos.putData(new Vector2f(0f, 0f));
        cornerPos.putData(new Vector2f(1f, 0f));
        cornerPos.putData(new Vector2f(0f, 1f));
        cornerPos.putData(new Vector2f(1f, 1f));
        
        model.finishVertexVBOs();
    }
}