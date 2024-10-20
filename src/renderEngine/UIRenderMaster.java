package renderEngine;

import java.util.LinkedList;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjglx.util.vector.Matrix3f;

import main.App;
import renderEngine.fonts.TextFont;
import shaders.ShaderProgram;
import sutil.math.SVector;

public class UIRenderMaster {

    public static final int FONT_TEXTURE_WIDTH = 256;
    public static final int FONT_TEXTURE_HEIGHT = 512;

    private App app;

    private ShaderProgram rectShader;
    private ShaderProgram textShader;
    private ShaderProgram imageShader;
    private ShaderProgram hslShader;
    private ShaderProgram activeShader;

    private Matrix3f uiMatrix;
    private LinkedList<Matrix3f> uiMatrixStack;

    private SVector fill;
    private boolean doFill;
    private SVector stroke;
    private boolean doStroke;
    private double strokeWeight;
    private TextFont textFont;
    private double textSize;

    private RawModel dummyVAO;
    // private RawModel quadVAO;

    public UIRenderMaster(App app) {
        this.app = app;

        textShader = new ShaderProgram("text", new String[] { "position", "textureCoords", "size" }, true);
        rectShader = new ShaderProgram("rect", null, true);
        imageShader = new ShaderProgram("image", null, true);
        hslShader = new ShaderProgram("hsl", null, true);

        dummyVAO = app.getLoader().loadToVAO(new double[] { 0, 0 });
        // quadVAO = app.getLoader().loadToVAO(new double[] { 0, 0, 0, 1, 1, 0, 1, 1 });

        uiMatrixStack = new LinkedList<>();
        textFont = null;
        fill = new SVector();
        stroke = new SVector();
    }

    public void start() {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        uiMatrixStack.clear();
        uiMatrix = new Matrix3f();
        fill(new SVector(0.5, 0.5, 0.5));
        stroke(new SVector());
        strokeWeight(1);

        activeShader = null;
    }

    public void rect(SVector position, SVector size) {
        activateShader(rectShader);

        rectShader.loadUniform("position", position);
        rectShader.loadUniform("size", size);
        rectShader.loadUniform("fill", fill);
        rectShader.loadUniform("doFill", doFill ? 1 : 0);
        rectShader.loadUniform("stroke", stroke);
        rectShader.loadUniform("doStroke", doStroke ? 1 : 0);
        rectShader.loadUniform("strokeWeight", strokeWeight);
        rectShader.loadUniform("uiMatrix", uiMatrix);
        rectShader.loadUniform("viewMatrix", createViewMatrix());

        GL30.glBindVertexArray(dummyVAO.getVaoID());
        GL11.glDrawArrays(GL11.GL_POINTS, 0, 1);
        GL30.glBindVertexArray(0);
    }

    public void text(String text, SVector position) {
        if (textFont == null) {
            return;
        }

        pushMatrix();
        translate(position);
        scale(textSize / textFont.getSize());

        activateShader(textShader);

        RawModel textVAO = textFont.generateVAO(text, app.getLoader());
        GL30.glBindVertexArray(textVAO.getVaoID());

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textFont.getTexture().getTextureID());
        // GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER,
        // GL30.GL_NEAREST_MIPMAP_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL30.GL_LINEAR_MIPMAP_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        Matrix3f viewMatrix = createViewMatrix();
        textShader.loadUniform("viewMatrix", viewMatrix);
        textShader.loadUniform("transformationMatrix", uiMatrix);
        textShader.loadUniform("fill", fill);
        textShader.loadUniform("doFill", doFill ? 1 : 0);

        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);
        GL20.glEnableVertexAttribArray(2);
        GL11.glDrawArrays(GL11.GL_POINTS, 0, textVAO.getVertexCount());
        GL20.glDisableVertexAttribArray(0);
        GL20.glDisableVertexAttribArray(1);
        GL20.glDisableVertexAttribArray(2);
        GL30.glBindVertexArray(0);

        popMatrix();
    }

    public void image(int textureID, SVector position, SVector size) {
        activateShader(imageShader);

        pushMatrix();
        translate(position);
        scale(size);

        imageShader.loadUniform("viewMatrix", createViewMatrix());
        imageShader.loadUniform("transformationMatrix", uiMatrix);
        // TODO
        SVector[] imageViewport = { new SVector(), new SVector(10000, 10000) };
        imageShader.loadUniform("viewportTopLeft", imageViewport[0]);
        imageShader.loadUniform("viewportBottomRight", imageViewport[1]);

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

        GL30.glBindVertexArray(dummyVAO.getVaoID());
        GL11.glDrawArrays(GL11.GL_POINTS, 0, 1);
        GL30.glBindVertexArray(0);

        // GL30.glBindVertexArray(quadVAO.getVaoID());
        // GL20.glEnableVertexAttribArray(0);
        // GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, quadVAO.getVertexCount());
        // GL20.glDisableVertexAttribArray(0);
        // GL30.glBindVertexArray(0);

        popMatrix();
    }

    public void hueSatField(SVector position, SVector size) {
        pushMatrix();
        translate(position);
        scale(size);

        activateShader(hslShader);
        hslShader.loadUniform("hueSat", 1);
        hslShader.loadUniform("viewMatrix", createViewMatrix());
        hslShader.loadUniform("transformationMatrix", uiMatrix);

        GL30.glBindVertexArray(dummyVAO.getVaoID());
        GL11.glDrawArrays(GL11.GL_POINTS, 0, 1);
        GL30.glBindVertexArray(0);

        popMatrix();
    }

    public void lightnessScale(SVector position, SVector size, double hue, double saturation) {
        pushMatrix();
        translate(position);
        scale(size);

        activateShader(hslShader);
        hslShader.loadUniform("hueSat", 0);
        hslShader.loadUniform("hue", hue);
        hslShader.loadUniform("saturation", saturation);
        hslShader.loadUniform("viewMatrix", createViewMatrix());
        hslShader.loadUniform("transformationMatrix", uiMatrix);

        GL30.glBindVertexArray(dummyVAO.getVaoID());
        GL11.glDrawArrays(GL11.GL_POINTS, 0, 1);
        GL30.glBindVertexArray(0);

        popMatrix();
    }

    private void activateShader(ShaderProgram shader) {
        if (activeShader == shader) {
            return;
        }
        if (activeShader != null) {
            activeShader.stop();
        }
        shader.start();
        activeShader = shader;
    }

    public void translate(SVector translation) {
        Matrix3f matrix = new Matrix3f();
        matrix.m20 = (float) translation.x;
        matrix.m21 = (float) translation.y;
        Matrix3f.mul(uiMatrix, matrix, uiMatrix);
    }

    public void scale(SVector scale) {
        Matrix3f matrix = new Matrix3f();
        matrix.m00 = (float) scale.x;
        matrix.m11 = (float) scale.y;
        Matrix3f.mul(uiMatrix, matrix, uiMatrix);
    }

    public void scale(double s) {
        Matrix3f matrix = new Matrix3f();
        matrix.m00 = (float) s;
        matrix.m11 = (float) s;
        Matrix3f.mul(uiMatrix, matrix, uiMatrix);
    }

    public void rotate(double angle) {
        Matrix3f matrix = new Matrix3f();
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);
        matrix.m00 = cos;
        matrix.m01 = -sin;
        matrix.m10 = sin;
        matrix.m11 = cos;
        Matrix3f.mul(uiMatrix, matrix, uiMatrix);
    }

    public void resetMatrix() {
        uiMatrix.setIdentity();
    }

    public void pushMatrix() {
        Matrix3f matrix = new Matrix3f();
        matrix.m00 = uiMatrix.m00;
        matrix.m10 = uiMatrix.m10;
        matrix.m20 = uiMatrix.m20;
        matrix.m01 = uiMatrix.m01;
        matrix.m11 = uiMatrix.m11;
        matrix.m21 = uiMatrix.m21;
        matrix.m02 = uiMatrix.m02;
        matrix.m12 = uiMatrix.m12;
        matrix.m22 = uiMatrix.m22;
        uiMatrixStack.push(matrix);
    }

    public void popMatrix() {
        uiMatrix = uiMatrixStack.pop();
    }

    public void fill(SVector fill) {
        this.fill.set(fill);
        doFill = true;
    }

    public void noFill() {
        doFill = false;
    }

    public void stroke(SVector stroke) {
        this.stroke.set(stroke);
        doStroke = true;
    }

    public void noStroke() {
        doStroke = false;
    }

    public void strokeWeight(double strokeWeight) {
        this.strokeWeight = strokeWeight;
    }

    public void textFont(TextFont font) {
        textFont = font;
        textSize = font.getSize();
    }

    public void textSize(double textSize) {
        this.textSize = textSize;
    }

    public void stop() {
        app.getLoader().textCleanUp();

        if (activeShader != null) {
            activeShader.stop();
        }
    }

    private Matrix3f createViewMatrix() {
        int[] displaySize = app.getWindow().getDisplaySize();
        Matrix3f matrix = new Matrix3f();
        matrix.m00 = 2f / displaySize[0];
        matrix.m11 = -2f / displaySize[1];
        matrix.m20 = -1;
        matrix.m21 = 1;
        return matrix;
    }
}