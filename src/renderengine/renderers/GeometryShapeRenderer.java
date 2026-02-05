package renderengine.renderers;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL31;
import org.lwjglx.util.vector.Matrix3f;

import renderengine.Loader;
import renderengine.drawcalls.DrawCall;
import renderengine.shaders.ShaderType;

public abstract class GeometryShapeRenderer<C extends DrawCall> extends ShapeRenderer<C> {

    public GeometryShapeRenderer(String name, Loader loader) {
        super(name, ShaderType.GEOMETRY, loader);
    }

    @Override
    public void render(Matrix3f viewMatrix) {
        shaderProgram.start();
        shaderProgram.loadUniform("viewMatrix", viewMatrix);

        model.bind();
        model.enableVBOs();
        while (prepareNextDrawcall())
            GL31.glDrawArraysInstanced(GL11.GL_POINTS, 0, 1, model.instanceCount());
        model.disableVBOs();

        shaderProgram.stop();
    }
}