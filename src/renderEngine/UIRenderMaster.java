package renderEngine;

import java.util.LinkedList;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjglx.util.vector.Matrix3f;
import org.lwjglx.util.vector.Vector3f;

import main.apps.App;
import renderEngine.fonts.TextFont;
import shaders.ShaderProgram;
import sutil.math.SVector;

public class UIRenderMaster {

    public static final int FONT_TEXTURE_WIDTH = 256;
    public static final int FONT_TEXTURE_HEIGHT = 512;

    private static final int NONE = 0, NORMAL = 1, CHECKERBOARD = 2;

    private App app;

    private ShaderProgram rectShader;
    private ShaderProgram textShader;
    private ShaderProgram imageShader;
    private ShaderProgram hslShader;
    private ShaderProgram ellipseShader;
    private ShaderProgram activeShader;

    private RawModel dummyVAO;

    private FrameBufferObject textFBO;

    private Matrix3f uiMatrix;
    private LinkedList<Matrix3f> uiMatrixStack;

    private ScissorInfo scissorInfo;
    private LinkedList<ScissorInfo> scissorStack;

    private double depth;

    private SVector fill;
    private double fillAlpha;
    private int fillMode;

    private double checkerboardSize;
    private SVector[] checkerboardColors;

    private SVector stroke;
    private int strokeMode;
    private double strokeWeight;

    private TextFont textFont;
    private double textSize;

    public UIRenderMaster(App app) {
        this.app = app;

        textShader = new ShaderProgram("text", new String[] { "position", "textureCoords", "size" }, true);
        rectShader = new ShaderProgram("rect", null, true);
        ellipseShader = new ShaderProgram("ellipse", null, true);
        imageShader = new ShaderProgram("image", null, true);
        hslShader = new ShaderProgram("hsl", null, true);

        Loader loader = app.getLoader();
        dummyVAO = loader.loadToVAO(new double[] { 0, 0 });
        textFBO = loader.createFBO(400, 400);

        uiMatrixStack = new LinkedList<>();
        scissorStack = new LinkedList<>();

        fill = new SVector();
        stroke = new SVector();

        checkerboardColors = new SVector[] { new SVector(), new SVector() };
    }

    public void start() {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL11.glEnable(GL13.GL_MULTISAMPLE);

        GL11.glDisable(GL11.GL_CULL_FACE);

        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(GL11.GL_LEQUAL);

        defaultFramebuffer();

        uiMatrixStack.clear();
        uiMatrix = new Matrix3f();

        scissorStack.clear();
        scissorInfo = new ScissorInfo();

        depth = 0;

        checkerboardFill(new SVector[] { new SVector(), new SVector() }, 1);
        fill(new SVector(0.5, 0.5, 0.5));
        fillAlpha(1.0);

        stroke(new SVector());
        strokeWeight(1);

        textFont = null;
        textSize = 1;

        activeShader = null;
    }

    public void stop() {
        app.getLoader().tempCleanUp();

        if (activeShader != null) {
            activeShader.stop();
        }
    }

    public void defaultFramebuffer() {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
    }

    public void textFramebuffer() {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, textFBO.fboID());
    }

    public void rect(SVector position, SVector size) {
        activateShader(rectShader);

        rectShader.loadUniform("position", position);
        rectShader.loadUniform("size", size);
        rectShader.loadUniform("fill", fill);
        rectShader.loadUniform("fillAlpha", fillAlpha);
        rectShader.loadUniform("fillMode", fillMode);
        rectShader.loadUniform("stroke", stroke);
        rectShader.loadUniform("strokeMode", strokeMode);
        rectShader.loadUniform("strokeWeight", strokeWeight);
        rectShader.loadUniform("checkerboardColor1", checkerboardColors[0]);
        rectShader.loadUniform("checkerboardColor2", checkerboardColors[1]);
        rectShader.loadUniform("checkerboardSize", checkerboardSize);
        rectShader.loadUniform("uiMatrix", uiMatrix);
        rectShader.loadUniform("viewMatrix", createViewMatrix());
        rectShader.loadUniform("depth", depth);

        GL30.glBindVertexArray(dummyVAO.getVaoID());
        GL11.glDrawArrays(GL11.GL_POINTS, 0, 1);
        GL30.glBindVertexArray(0);
    }

    public void ellipse(SVector position, SVector size) {
        activateShader(ellipseShader);

        pushMatrix();
        translate(position);
        scale(size);

        ellipseShader.loadUniform("fill", fill);
        ellipseShader.loadUniform("uiMatrix", uiMatrix);
        ellipseShader.loadUniform("viewMatrix", createViewMatrix());
        ellipseShader.loadUniform("depth", depth);

        GL30.glBindVertexArray(dummyVAO.getVaoID());
        GL11.glDrawArrays(GL11.GL_POINTS, 0, 1);
        GL30.glBindVertexArray(0);

        popMatrix();
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
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textFont.getTextureID());
        // GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER,
        // GL30.GL_NEAREST_MIPMAP_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL30.GL_LINEAR_MIPMAP_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        Matrix3f viewMatrix = createViewMatrix();
        textShader.loadUniform("viewMatrix", viewMatrix);
        textShader.loadUniform("transformationMatrix", uiMatrix);
        textShader.loadUniform("depth", depth);
        textShader.loadUniform("fill", fill);
        textShader.loadUniform("doFill", fillMode == NONE ? 0 : 1);

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

        // activate alpha blending

        pushMatrix();
        translate(position);
        scale(size);

        imageShader.loadUniform("viewMatrix", createViewMatrix());
        imageShader.loadUniform("transformationMatrix", uiMatrix);

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

        GL30.glBindVertexArray(dummyVAO.getVaoID());
        GL11.glDrawArrays(GL11.GL_POINTS, 0, 1);
        GL30.glBindVertexArray(0);

        popMatrix();
    }

    public void hueSatField(SVector position, SVector size, boolean circular, boolean hsl) {
        pushMatrix();
        translate(position);
        scale(size);

        activateShader(hslShader);
        hslShader.loadUniform("hueSatAlpha", circular ? 3 : 1);
        hslShader.loadUniform("hsv", hsl ? 0 : 1);
        hslShader.loadUniform("viewMatrix", createViewMatrix());
        hslShader.loadUniform("transformationMatrix", uiMatrix);
        hslShader.loadUniform("depth", depth);

        GL30.glBindVertexArray(dummyVAO.getVaoID());
        GL11.glDrawArrays(GL11.GL_POINTS, 0, 1);
        GL30.glBindVertexArray(0);

        popMatrix();
    }

    public void lightnessScale(SVector position, SVector size, double hue, double saturation, int orientation,
            boolean hsl) {
        pushMatrix();
        translate(position);
        scale(size);

        activateShader(hslShader);
        hslShader.loadUniform("hueSatAlpha", 0);
        hslShader.loadUniform("hsv", hsl ? 0 : 1);
        hslShader.loadUniform("orientation", orientation);
        hslShader.loadUniform("hue", hue);
        hslShader.loadUniform("saturation", saturation);
        hslShader.loadUniform("viewMatrix", createViewMatrix());
        hslShader.loadUniform("transformationMatrix", uiMatrix);
        hslShader.loadUniform("depth", depth);

        GL30.glBindVertexArray(dummyVAO.getVaoID());
        GL11.glDrawArrays(GL11.GL_POINTS, 0, 1);
        GL30.glBindVertexArray(0);

        popMatrix();
    }

    public void alphaScale(SVector position, SVector size, int orientation) {
        pushMatrix();
        translate(position);
        scale(size);

        activateShader(hslShader);
        hslShader.loadUniform("hueSatAlpha", 2);
        hslShader.loadUniform("orientation", orientation);
        hslShader.loadUniform("viewMatrix", createViewMatrix());
        hslShader.loadUniform("transformationMatrix", uiMatrix);
        hslShader.loadUniform("depth", depth);
        hslShader.loadUniform("fill", fill);

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

    public void scissor(SVector position, SVector size) {
        scissor(position, size, true);
    }

    public void scissor(SVector position, SVector size, boolean clipToPrevScissor) {
        Vector3f pos3f = new Vector3f((float) position.x, (float) position.y, 1);
        Matrix3f.transform(uiMatrix, pos3f, pos3f);

        Vector3f size3f = new Vector3f((float) size.x, (float) size.y, 0);
        Matrix3f.transform(uiMatrix, size3f, size3f);

        // not entirely sure why, but this functions returns 4 ints (0, 0, w, h)
        int[] dims = new int[4];
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, dims);

        int w = (int) size3f.x, h = (int) size3f.y;
        int x = (int) pos3f.x, y = dims[3] - h - (int) pos3f.y;

        // no need to clip if the previous scissor was disabled
        if (clipToPrevScissor && scissorInfo.enabled) {
            int x0 = scissorInfo.x,
                    y0 = scissorInfo.y,
                    w0 = scissorInfo.w,
                    h0 = scissorInfo.h;

            // left
            if (x < x0) {
                w -= x0 - x;
                x = x0;
            }
            // right
            if (x + w > x0 + w0) {
                w = x0 + w0 - x;
            }
            // top
            if (y < y0) {
                h -= y0 - y;
                y = y0;
            }
            // bottom
            if (y + h > y0 + h0) {
                h = y0 + h0 - y;
            }
        }

        scissorInfo.set(x, y, w, h);

        scissor(x, y, w, h);
    }

    private void scissor(int x, int y, int w, int h) {
        if (w < 0) {
            w = 0;
        }
        if (h < 0) {
            h = 0;
        }
        GL11.glScissor(x, y, w, h);

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
    }

    public void noScissor() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        scissorInfo.clear();
    }

    public void pushScissor() {
        scissorStack.add(new ScissorInfo(scissorInfo));
    }

    public void popScissor() {
        scissorInfo = scissorStack.removeLast();

        if (scissorInfo.enabled) {
            scissor(scissorInfo.x, scissorInfo.y, scissorInfo.w, scissorInfo.h);
        } else {
            noScissor();
        }
    }

    public void depth(double depth) {
        this.depth = depth;
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }

    public void noDepth() {
        GL11.glDisable(GL11.GL_DEPTH_TEST);
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

    public void fillAlpha(double alpha) {
        this.fillAlpha = alpha;
    }

    public void fill(SVector fill) {
        this.fill.set(fill);
        fillMode = NORMAL;
    }

    public void noFill() {
        fillMode = NONE;
    }

    public void checkerboardFill(SVector[] colors, double size) {
        fillMode = CHECKERBOARD;
        checkerboardColors[0].set(colors[0]);
        checkerboardColors[1].set(colors[1]);
        checkerboardSize = size;
    }

    public void stroke(SVector stroke) {
        this.stroke.set(stroke);
        strokeMode = NORMAL;
    }

    public void noStroke() {
        strokeMode = NONE;
    }

    public void strokeWeight(double strokeWeight) {
        this.strokeWeight = strokeWeight;
    }

    public void checkerboardStroke(SVector[] colors, double size) {
        strokeMode = CHECKERBOARD;
        checkerboardColors[0].set(colors[0]);
        checkerboardColors[1].set(colors[1]);
        checkerboardSize = size;
    }

    public void textFont(TextFont font) {
        textFont = font;
        textSize = font.getSize();
    }

    public void textSize(double textSize) {
        this.textSize = textSize;
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

    private class ScissorInfo {

        boolean enabled;
        int x, y, w, h;

        ScissorInfo() {
            clear();
        }

        public ScissorInfo(ScissorInfo other) {
            this.enabled = other.enabled;
            this.x = other.x;
            this.y = other.y;
            this.w = other.w;
            this.h = other.h;
        }

        public void set(int x, int y, int w, int h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            enabled = true;
        }

        public void clear() {
            enabled = false;
            x = y = w = h = 0;
        }
    }
}