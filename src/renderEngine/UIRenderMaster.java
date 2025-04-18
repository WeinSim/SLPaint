package renderEngine;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjglx.util.vector.Matrix3f;
import org.lwjglx.util.vector.Vector3f;

import main.apps.App;
import main.apps.MainApp;
import renderEngine.drawcalls.ClipAreaInfo;
import renderEngine.drawcalls.RectFillData;
import renderEngine.drawcalls.RectFillDrawCall;
import renderEngine.drawcalls.RectFillDrawCallList;
import renderEngine.drawcalls.RectFillVAO;
import renderEngine.drawcalls.TextData;
import renderEngine.drawcalls.TextDrawCall;
import renderEngine.drawcalls.TextDrawCallList;
import renderEngine.drawcalls.TextVAO;
import renderEngine.fonts.TextFont;
import renderEngine.shaders.ShaderProgram;
import sutil.math.SVector;

public class UIRenderMaster {

    public static final int MAX_FONT_ATLASSES = 4;
    public static final int MAX_FONT_CHARS = 256;
    public static final int MAX_TEXT_DATA = 256;

    private static final int NONE = 0, NORMAL = 1, CHECKERBOARD = 2;

    private App app;
    private Loader loader;

    private ShaderProgram rectFillShader,
            rectOutlineShader,
            textShader,
            imageShader,
            hslShader,
            ellipseShader,
            activeShader;

    private HashMap<TextFont, TextDrawCallList> textDrawCalls;
    private RectFillDrawCallList rectFillDrawCalls;

    private RawModel dummyVAO;

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
    private int strokeMode;
    private double strokeWeight;

    private TextFont textFont;
    private double textSize;

    public UIRenderMaster(App app, Loader loader) {
        this.app = app;
        this.loader = loader;

        textShader = new ShaderProgram(
                "text",
                new String[] { "charIndex", "position", "textSize", "color" },
                true);
        rectOutlineShader = new ShaderProgram("rectOutline", null, true);
        rectFillShader = new ShaderProgram("rectFill", null, true);
        ellipseShader = new ShaderProgram("ellipse", null, true);
        imageShader = new ShaderProgram("image", null, true);
        hslShader = new ShaderProgram("hsl", null, true);

        dummyVAO = new RawModel(0, 1);
        if (app instanceof MainApp mainApp) {
            textFBO = loader.createFBO(mainApp.getImage().getWidth(), mainApp.getImage().getHeight());
        }

        textDrawCalls = new HashMap<>();
        rectFillDrawCalls = new RectFillDrawCallList();

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
        strokeWeight(1);

        textFont = null;
        textSize = 1;

        activeShader = null;
    }

    public void stop() {
        // Rendering the rectangle first in order to allow (transparent) text to render
        // above the background rectangles
        renderGiantRectVAO();
        renderGiantTextVAO();

        textDrawCalls.clear();
        rectFillDrawCalls = new RectFillDrawCallList();

        loader.tempCleanUp();

        if (activeShader != null) {
            activeShader.stop();
        }
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
        if (fillMode > 0) {
            SVector transformedPosition = new SVector(
                    uiMatrix.m00 * position.x + uiMatrix.m10 * position.y + uiMatrix.m20,
                    uiMatrix.m01 * position.x + uiMatrix.m11 * position.y + uiMatrix.m21);
            SVector transformedSize = new SVector(uiMatrix.m00 * size.x + uiMatrix.m10 * size.y,
                    uiMatrix.m01 * size.x + uiMatrix.m11 * size.y);

            rectFillDrawCalls.addDrawCall(
                    new RectFillDrawCall(transformedPosition, transformedSize, depth,
                            fillMode == NORMAL ? new SVector(fill) : checkerboardColors[0], fillAlpha),
                    new RectFillData(new ClipAreaInfo(clipAreaInfo), fillMode == CHECKERBOARD, checkerboardColors[1],
                            checkerboardSize));
        }

        if (strokeMode > 0) {
            GL30.glBindVertexArray(dummyVAO.vaoID());

            activateShader(rectOutlineShader);

            rectOutlineShader.loadUniform("position", position);
            rectOutlineShader.loadUniform("size", size);
            rectOutlineShader.loadUniform("uiMatrix", uiMatrix);
            rectOutlineShader.loadUniform("viewMatrix", createViewMatrix());
            rectOutlineShader.loadUniform("depth", depth);

            rectOutlineShader.loadUniform("strokeWeight", strokeWeight);
            rectOutlineShader.loadUniform("applyCheckerboard", strokeMode == CHECKERBOARD ? 1 : 0);

            if (strokeMode == NORMAL) {
                // normal stroke
                rectOutlineShader.loadUniform("color1", stroke);
            } else {
                // checkerboard stroke
                rectOutlineShader.loadUniform("color1", checkerboardColors[0]);
                rectOutlineShader.loadUniform("color2", checkerboardColors[1]);
                rectOutlineShader.loadUniform("checkerboardSize", checkerboardSize);
            }

            GL11.glDrawArrays(GL11.GL_POINTS, 0, 1);
        }

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

        GL30.glBindVertexArray(dummyVAO.vaoID());
        GL11.glDrawArrays(GL11.GL_POINTS, 0, 1);
        GL30.glBindVertexArray(0);

        popMatrix();
    }

    public void text(String text, SVector position) {
        if (textFont == null) {
            return;
        }
        if (fillMode == NONE) {
            return;
        }

        TextDrawCallList drawCalls = textDrawCalls.get(textFont);
        if (drawCalls == null) {
            drawCalls = new TextDrawCallList();
            textDrawCalls.put(textFont, drawCalls);
        }

        pushMatrix();
        translate(position);
        scale(textSize / textFont.getSize());

        drawCalls.addDrawCall(
                new TextDrawCall(text, Matrix3f.load(uiMatrix, null), depth),
                new TextData(new ClipAreaInfo(clipAreaInfo), fill.copy(), textSize / textFont.getSize()));

        popMatrix();
    }

    private void renderGiantTextVAO() {
        activateShader(textShader);

        for (Entry<TextFont, TextDrawCallList> entry : textDrawCalls.entrySet()) {
            TextFont font = entry.getKey();

            // load textures and other font data
            int[] textureIDs = font.getTextureIDs();
            for (int i = 0; i < textureIDs.length; i++) {
                GL13.glActiveTexture(GL13.GL_TEXTURE0 + i);
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureIDs[i]);
                textShader.loadUniform(String.format("textureSamplers[%d]", i), i);
            }
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL30.GL_LINEAR_MIPMAP_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

            Matrix3f viewMatrix = createViewMatrix();
            textShader.loadUniform("viewMatrix", viewMatrix);
            textShader.loadUniform("textureSize", new SVector(font.getTextureWidth(), font.getTextureHeight()));

            textShader.setUniformBlockData("FontData", font.getUBOData());
            textShader.syncUniformBlock("FontData", loader);

            TextDrawCallList drawCalls = entry.getValue();
            for (TextVAO textVAO : drawCalls.getVAOs()) {
                RawModel model = font.createGiantVAO(textVAO, loader);

                textShader.setUniformBlockData("TextData", textVAO.getUBOData());
                textShader.syncUniformBlock("TextData", loader);

                GL30.glBindVertexArray(model.vaoID());
                GL20.glEnableVertexAttribArray(0);
                GL20.glEnableVertexAttribArray(1);
                GL20.glEnableVertexAttribArray(2);

                GL11.glDrawArrays(GL11.GL_POINTS, 0, model.vertexCount());

                GL20.glDisableVertexAttribArray(0);
                GL20.glDisableVertexAttribArray(1);
                GL20.glDisableVertexAttribArray(2);
            }
        }
    }

    private void renderGiantRectVAO() {
        activateShader(rectFillShader);
        rectFillShader.loadUniform("viewMatrix", createViewMatrix());

        for (RectFillVAO vao : rectFillDrawCalls.getVAOs()) {

            rectFillShader.setUniformBlockData("RectFillData", vao.getUBOData());
            rectFillShader.syncUniformBlock("RectFillData", loader);

            int vertexCount = vao.getVertexCount();
            float[] positions = new float[vertexCount * 3];
            float[] sizes = new float[vertexCount * 2];
            float[] color1 = new float[vertexCount * 4];
            int[] dataIndices = new int[vertexCount];
            int i = 0;
            for (RectFillDrawCall drawCall : vao.getDrawCalls()) {

                positions[3 * i] = (float) drawCall.getPosition().x;
                positions[3 * i + 1] = (float) drawCall.getPosition().y;
                positions[3 * i + 2] = (float) drawCall.getDepth();

                sizes[2 * i] = (float) drawCall.getSize().x;
                sizes[2 * i + 1] = (float) drawCall.getSize().y;

                color1[4 * i] = (float) drawCall.getColor1().x;
                color1[4 * i + 1] = (float) drawCall.getColor1().y;
                color1[4 * i + 2] = (float) drawCall.getColor1().z;
                color1[4 * i + 3] = (float) drawCall.getAlpha();

                dataIndices[i] = drawCall.getDataIndex();

                i++;
            }

            RawModel model = loader.loadRectVAO(positions, sizes, color1, dataIndices);
            GL30.glBindVertexArray(model.vaoID());

            GL20.glEnableVertexAttribArray(0);
            GL20.glEnableVertexAttribArray(1);
            GL20.glEnableVertexAttribArray(2);
            GL20.glEnableVertexAttribArray(3);

            GL11.glDrawArrays(GL11.GL_POINTS, 0, model.vertexCount());

            GL20.glDisableVertexAttribArray(0);
            GL20.glDisableVertexAttribArray(1);
            GL20.glDisableVertexAttribArray(2);
            GL20.glDisableVertexAttribArray(3);
        }
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

        GL30.glBindVertexArray(dummyVAO.vaoID());
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

        GL30.glBindVertexArray(dummyVAO.vaoID());
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

        GL30.glBindVertexArray(dummyVAO.vaoID());
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

        GL30.glBindVertexArray(dummyVAO.vaoID());
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

    // public static record TextDrawCall(String text, Matrix3f transformationMatrix,
    // double depth, int textDataIndex) {
    // }

    // public static record TextData(SVector color, double textSize, ClipAreaInfo
    // clipArea) {
    // }

    // public static class TextDrawCallList {

    // private TextFont font;

    // private ArrayList<TextVAO> textVAOs;

    // public long totalNanos;

    // public TextDrawCallList(TextFont font) {
    // this.font = font;

    // textVAOs = new ArrayList<>();
    // totalNanos = 0;
    // }

    // public void addDrawCall(String text, Matrix3f transformationMatrix, double
    // depth, SVector color,
    // double textSize, ClipAreaInfo clipArea) {

    // TextData textData = new TextData(color, textSize, clipArea);

    // // Determine a TextVAO that already contains the correct text data.
    // TextVAO vao = null;
    // int textDataIndex = -1;
    // for (TextVAO textVAO : textVAOs) {
    // textDataIndex = textVAO.getTextDataIndex(textData);
    // if (textDataIndex != -1) {
    // vao = textVAO;
    // break;
    // }
    // }
    // if (vao == null) {
    // // No VAO has the correct text data. Create new one if neccessary.
    // boolean createNewVAO = true;
    // if (!textVAOs.isEmpty()) {
    // vao = textVAOs.getLast();
    // createNewVAO = !vao.hasRemainingCapacity();
    // }
    // if (createNewVAO) {
    // vao = new TextVAO(font);
    // textVAOs.add(vao);
    // }
    // }
    // if (textDataIndex == -1) {
    // // The text data already exists in some VAO.
    // vao.addDrawCall(text, transformationMatrix, depth, textData);
    // } else {
    // // A new array index needs to be allocated for the text data.
    // vao.addDrawCall(text, transformationMatrix, depth, textDataIndex);
    // }
    // }
    // }

    // public static class TextVAO {

    // public LinkedList<TextDrawCall> drawCalls;
    // public int totalLength;

    // public TextData[] textDataArray;
    // public int textDataLength;

    // private TextFont font;

    // public TextVAO(TextFont font) {
    // this.font = font;

    // drawCalls = new LinkedList<>();
    // totalLength = 0;

    // textDataArray = new TextData[MAX_TEXT_DATA];
    // textDataLength = 0;
    // }

    // public int getTextDataIndex(TextData textData) {
    // for (int i = 0; i < textDataLength; i++) {
    // if (textDataArray[i].equals(textData)) {
    // return i;
    // }
    // }
    // return -1;
    // }

    // public boolean hasRemainingCapacity() {
    // return textDataLength < MAX_TEXT_DATA;
    // }

    // public void addDrawCall(String text, Matrix3f transformationMatrix, double
    // depth, TextData textData) {
    // int textDataIndex = textDataLength;
    // textDataArray[textDataLength++] = textData;
    // addDrawCall(text, transformationMatrix, depth, textDataIndex);
    // }

    // public void addDrawCall(String text, Matrix3f transformationMatrix, double
    // depth, int textDataIndex) {
    // // this method is slow af
    // // SUtil.addSorted(drawCalls, new TextDrawCall(text, transformationMatrix,
    // // depth, textDataIndex), true);
    // drawCalls.add(new TextDrawCall(text, transformationMatrix, depth,
    // textDataIndex));
    // totalLength += text.length();
    // }

    // public FloatBuffer getUBOData() {
    // FloatBuffer buffer = BufferUtils.createFloatBuffer(8 * MAX_TEXT_DATA);
    // for (int i = 0; i < textDataLength; i++) {
    // SVector color = textDataArray[i].color();
    // buffer.put((float) color.x);
    // buffer.put((float) color.y);
    // buffer.put((float) color.z);

    // buffer.put((float) (textDataArray[i].textSize() / font.getSize()));
    // }
    // for (int i = 0; i < MAX_TEXT_DATA - textDataLength; i++) {
    // buffer.put(0f);
    // buffer.put(0f);
    // buffer.put(0f);
    // buffer.put(0f);
    // }
    // for (int i = 0; i < textDataLength; i++) {
    // ClipAreaInfo clipArea = textDataArray[i].clipArea();

    // SVector position = clipArea.enabled ? clipArea.position : new SVector(0, 0);
    // buffer.put((float) position.x);
    // buffer.put((float) position.y);

    // SVector size = clipArea.enabled ? clipArea.size : new SVector(100000,
    // 100000);
    // buffer.put((float) (position.x + size.x));
    // buffer.put((float) (position.y + size.y));
    // }
    // for (int i = 0; i < MAX_TEXT_DATA - textDataLength; i++) {
    // buffer.put(0f);
    // buffer.put(0f);
    // buffer.put(0f);
    // buffer.put(0f);
    // }
    // buffer.flip();
    // return buffer;
    // }
    // }
}