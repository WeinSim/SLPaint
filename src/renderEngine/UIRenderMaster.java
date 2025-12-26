package renderEngine;

import java.util.ArrayList;
import java.util.LinkedList;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjglx.util.vector.Matrix3f;
import org.lwjglx.util.vector.Vector3f;

import main.apps.App;
import main.apps.MainApp;
import renderEngine.fonts.TextFont;
import renderEngine.shaders.ShaderProgram;
import renderEngine.shaders.bufferobjects.FrameBufferObject;
import renderEngine.shaders.drawcalls.EllipseCollector;
import renderEngine.shaders.drawcalls.EllipseDrawCall;
import renderEngine.shaders.drawcalls.HSLCollector;
import renderEngine.shaders.drawcalls.HSLDrawCall;
import renderEngine.shaders.drawcalls.ImageCollector;
import renderEngine.shaders.drawcalls.ImageDrawCall;
import renderEngine.shaders.drawcalls.RectFillCollector;
import renderEngine.shaders.drawcalls.RectFillDrawCall;
import renderEngine.shaders.drawcalls.RectOutlineCollector;
import renderEngine.shaders.drawcalls.RectOutlineDrawCall;
import renderEngine.shaders.drawcalls.ShapeCollector;
import renderEngine.shaders.drawcalls.TextCollector;
import renderEngine.shaders.drawcalls.TextDrawCall;
import sutil.math.SVector;

public class UIRenderMaster {

    public static final int MAX_FONT_ATLASSES = 4;
    public static final int MAX_FONT_CHARS = 256;
    public static final int MAX_TEXT_DATA = 256;

    private static final int NONE = 0, NORMAL = 1, CHECKERBOARD = 2;

    private App app;
    private Loader loader;

    private ArrayList<ShapeCollector<?>> shapeCollectors;
    private RectFillCollector rectFillCollector;
    private RectOutlineCollector rectOutlineCollector;
    private HSLCollector hslCollector;
    private EllipseCollector ellipseCollector;
    private TextCollector textCollector;
    private ImageCollector imageCollector;

    private FrameBufferObject textFBO;

    private FrameBufferObject currentFramebuffer;

    private Matrix3f uiMatrix;
    private LinkedList<Matrix3f> uiMatrixStack;

    private ClipAreaInfo clipAreaInfo;
    private LinkedList<ClipAreaInfo> clipAreaStack;

    private double depth;

    private SVector fill;
    private double fillAlpha;
    private int fillMode;

    private double checkerboardSize;
    private SVector[] checkerboardColors;

    private SVector stroke;
    private double strokeAlpha;
    private int strokeMode;
    private double strokeWeight;

    private TextFont textFont;
    private double textSize;

    public UIRenderMaster(App app, Loader loader) {
        this.app = app;
        this.loader = loader;

        shapeCollectors = new ArrayList<>();

        rectFillCollector = new RectFillCollector();
        shapeCollectors.add(rectFillCollector);

        rectOutlineCollector = new RectOutlineCollector();
        shapeCollectors.add(rectOutlineCollector);

        hslCollector = new HSLCollector();
        shapeCollectors.add(hslCollector);

        ellipseCollector = new EllipseCollector();
        shapeCollectors.add(ellipseCollector);

        textCollector = new TextCollector(loader);
        shapeCollectors.add(textCollector);

        imageCollector = new ImageCollector();
        shapeCollectors.add(imageCollector);

        if (app instanceof MainApp mainApp) {
            textFBO = loader.createFBO(mainApp.getImage().getWidth(), mainApp.getImage().getHeight());
        }

        uiMatrixStack = new LinkedList<>();
        clipAreaStack = new LinkedList<>();

        fill = new SVector();
        stroke = new SVector();

        checkerboardColors = new SVector[] { new SVector(), new SVector() };
    }

    public void start() {
        GL11.glEnable(GL11.GL_BLEND);
        GL30.glBlendFuncSeparate(
                GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, // rgb
                GL11.GL_ONE_MINUS_DST_ALPHA, GL11.GL_ONE); // alpha

        GL11.glEnable(GL13.GL_MULTISAMPLE);

        GL11.glDisable(GL11.GL_CULL_FACE);

        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        GL11.glDepthMask(true);

        defaultFramebuffer();

        uiMatrixStack.clear();
        uiMatrix = new Matrix3f();

        clipAreaStack.clear();
        clipAreaInfo = new ClipAreaInfo();

        depth = 0;

        checkerboardFill(new SVector[] { new SVector(), new SVector() }, 1);
        fill(new SVector(0.5, 0.5, 0.5));
        fillAlpha(1.0);

        stroke(new SVector());
        strokeAlpha(1.0);
        strokeWeight(1);

        textFont = null;
        textSize = 1;
    }

    public void stop() {
        GL11.glEnable(GL11.GL_DEPTH_TEST);

        Matrix3f viewMatrix = createViewMatrix();
        for (ShapeCollector<?> shapeCollector : shapeCollectors) {
            ShaderProgram shader = shapeCollector.getShaderProgram();
            shader.start();
            shader.loadUniform("viewMatrix", viewMatrix);

            RawModel model;
            while ((model = shapeCollector.getNextRawModel(loader)) != null) {
                GL30.glBindVertexArray(model.vaoID());

                for (int i = 0; i < model.numAttributes(); i++)
                    GL20.glEnableVertexAttribArray(i);

                shapeCollector.prepare();

                GL11.glDrawArrays(GL11.GL_POINTS, 0, model.vertexCount());

                for (int i = 0; i < model.numAttributes(); i++)
                    GL20.glDisableVertexAttribArray(i);
            }

            shader.stop();
        }

        loader.tempCleanUp();
    }

    public void defaultFramebuffer() {
        framebuffer(null);
    }

    public void textFramebuffer() {
        framebuffer(textFBO);
    }

    public void framebuffer(FrameBufferObject framebuffer) {
        int fboID = framebuffer == null ? 0 : framebuffer.fboID();
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fboID);

        int[] size = framebuffer == null
                ? app.getWindow().getDisplaySize()
                : new int[] { framebuffer.width(), framebuffer.height() };
        GL11.glViewport(0, 0, size[0], size[1]);

        currentFramebuffer = framebuffer;
    }

    public void setBGColor(SVector bgColor) {
        setBGColor(bgColor, 1.0);
    }

    public void setBGColor(SVector bgColor, double alpha) {
        GL11.glClearColor((float) bgColor.x, (float) bgColor.y, (float) bgColor.z, (float) alpha);
        GL11.glClearDepth(1.0);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
    }

    public void rect(SVector position, SVector size) {
        if (fillMode > 0)
            rectFillCollector.addShape(
                    new RectFillDrawCall(position, depth, size,
                            new Matrix3f().load(uiMatrix),
                            // uiMatrix,
                            new ClipAreaInfo(clipAreaInfo),
                            new SVector(fillMode == NORMAL ? fill : checkerboardColors[0]), fillAlpha,
                            new SVector(checkerboardColors[1]), checkerboardSize, fillMode == CHECKERBOARD));

        if (strokeMode > 0)
            rectOutlineCollector.addShape(
                    new RectOutlineDrawCall(position, depth, size,
                            new Matrix3f().load(uiMatrix),
                            // uiMatrix,
                            new ClipAreaInfo(clipAreaInfo),
                            new SVector(strokeMode == NORMAL ? stroke : checkerboardColors[0]), strokeAlpha,
                            strokeWeight, new SVector(checkerboardColors[1]), checkerboardSize,
                            fillMode == CHECKERBOARD));
    }

    public void ellipse(SVector position, SVector size) {
        if (fillMode != NORMAL)
            return;

        ellipseCollector.addShape(new EllipseDrawCall(position, depth, size, new Matrix3f().load(uiMatrix),
                new ClipAreaInfo(clipAreaInfo), new SVector(fill), fillAlpha));
    }

    public void text(String text, SVector position) {
        if (textFont == null)
            return;

        if (fillMode != NORMAL)
            return;

        textCollector.addShape(
                new TextDrawCall(position, depth, textSize / textFont.getSize(), new Matrix3f().load(uiMatrix),
                        new ClipAreaInfo(clipAreaInfo), new SVector(fill), fillAlpha, text, textFont));
    }

    public void image(int textureID, SVector position, SVector size) {
        imageCollector.addShape(new ImageDrawCall(position, depth, size, new Matrix3f().load(uiMatrix),
                new ClipAreaInfo(clipAreaInfo), textureID));
    }

    public void hueSatField(SVector position, SVector size, boolean circular, boolean hsl) {
        int flags = (circular ? HSLDrawCall.HUE_SAT_FIELD_CIRC : HSLDrawCall.HUE_SAT_FIELD_RECT)
                | (hsl ? HSLDrawCall.HSL : HSLDrawCall.HSV);
                // System.out.println(depth);
        hslCollector.addShape(new HSLDrawCall(position, depth, size, new Matrix3f().load(uiMatrix),
                new ClipAreaInfo(clipAreaInfo), new SVector(), flags));
    }

    public void lightnessScale(SVector position, SVector size, double hue, double saturation, boolean vertical,
            boolean hsl) {

        int flags = HSLDrawCall.LIGHTNESS_SCALE
                | (hsl ? HSLDrawCall.HSL : HSLDrawCall.HSV)
                | (vertical ? HSLDrawCall.VERTICAL : HSLDrawCall.HORIZONTAL);
        hslCollector.addShape(new HSLDrawCall(position, depth, size, new Matrix3f().load(uiMatrix),
                new ClipAreaInfo(clipAreaInfo), new SVector(hue, saturation, 0), flags));
    }

    public void alphaScale(SVector position, SVector size, boolean vertical) {
        int flags = HSLDrawCall.ALPHA_SCALE
                | (vertical ? HSLDrawCall.VERTICAL : HSLDrawCall.HORIZONTAL);
        hslCollector.addShape(new HSLDrawCall(position, depth, size, new Matrix3f().load(uiMatrix),
                new ClipAreaInfo(clipAreaInfo), new SVector(fill), flags));
    }

    public void clipArea(SVector position, SVector size) {
        clipArea(position, size, true);
    }

    public void clipArea(SVector position, SVector size, boolean clipToPrevClipArea) {
        Vector3f pos3f = new Vector3f((float) position.x, (float) position.y, 1);
        Matrix3f.transform(uiMatrix, pos3f, pos3f);

        Vector3f size3f = new Vector3f((float) size.x, (float) size.y, 0);
        Matrix3f.transform(uiMatrix, size3f, size3f);

        double x = pos3f.x, y = pos3f.y,
                w = size3f.x, h = size3f.y;

        // no need to clip if the previous clip area was disabled
        if (clipToPrevClipArea && clipAreaInfo.isEnabled()) {
            double x0 = clipAreaInfo.getPosition().x,
                    y0 = clipAreaInfo.getPosition().y,
                    w0 = clipAreaInfo.getSize().x,
                    h0 = clipAreaInfo.getSize().y;

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

        clipAreaInfo.set(new SVector(x, y), new SVector(w, h));
    }

    public void noClipArea() {
        clipAreaInfo.clear();
    }

    public void pushClipArea() {
        clipAreaStack.add(new ClipAreaInfo(clipAreaInfo));
    }

    public void popClipArea() {
        clipAreaInfo = clipAreaStack.removeLast();
    }

    public void depth(double depth) {
        this.depth = depth;
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

    public void fillAlpha(double alpha) {
        this.fillAlpha = alpha;
    }

    public void stroke(SVector stroke) {
        this.stroke.set(stroke);
        strokeMode = NORMAL;
    }

    public void noStroke() {
        strokeMode = NONE;
    }

    public void checkerboardStroke(SVector[] colors, double size) {
        strokeMode = CHECKERBOARD;
        checkerboardColors[0].set(colors[0]);
        checkerboardColors[1].set(colors[1]);
        checkerboardSize = size;
    }

    public void strokeAlpha(double strokeAlpha) {
        this.strokeAlpha = strokeAlpha;
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

    private Matrix3f createViewMatrix() {
        Matrix3f matrix = new Matrix3f();
        int width, height;
        float sign = currentFramebuffer == null ? -1 : 1;
        if (currentFramebuffer == null) {
            int[] displaySize = app.getWindow().getDisplaySize();
            width = displaySize[0];
            height = displaySize[1];
        } else {
            width = currentFramebuffer.width();
            height = currentFramebuffer.height();
        }
        matrix.m00 = 2f / width;
        matrix.m11 = sign * 2f / height;
        matrix.m20 = -1;
        matrix.m21 = -sign;
        return matrix;
    }

    public FrameBufferObject getTextFBO() {
        return textFBO;
    }
}