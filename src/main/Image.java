package main;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.nio.IntBuffer;
import java.util.Arrays;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import sutil.SUtil;

public class Image {

    private int textureID;

    private BufferedImage bufferedImage;
    private int[] pixelArray; // the backing array of bufferedImage
    private int width, height;

    private boolean dirty;
    private int dirtyMinX;
    private int dirtyMaxX;
    private int dirtyMinY;
    private int dirtyMaxY;

    public Image(BufferedImage image) {
        setBufferedImage(image);

        textureID = GL11.glGenTextures();

        updateOpenGLTexture(false);
    }

    private void setBufferedImage(BufferedImage image) {
        if (image.getType() != BufferedImage.TYPE_INT_ARGB) {
            throw new RuntimeException("Wrong image type! Only TYPE_INT_ARGB is supported.");
        }

        bufferedImage = image;
        pixelArray = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        width = image.getWidth();
        height = image.getHeight();
    }

    public void updateOpenGLTexture() {
        if (dirty) {
            updateOpenGLTexture(true);
        }
    }

    public void updateOpenGLTexture(boolean subArea) {
        IntBuffer buffer = BufferUtils.createIntBuffer(width * height);
        buffer.put(pixelArray);
        buffer.flip();

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);

        if (subArea) {
            int dirtyWidth = dirtyMaxX - dirtyMinX + 1,
                    dirtyHeight = dirtyMaxY - dirtyMinY + 1;

            GL11.glPixelStorei(GL11.GL_UNPACK_ROW_LENGTH, width);
            GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_PIXELS, dirtyMinX);
            GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_ROWS, dirtyMinY);

            GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, dirtyMinX, dirtyMinY, dirtyWidth, dirtyHeight, GL12.GL_BGRA,
                    GL12.GL_UNSIGNED_INT_8_8_8_8_REV, buffer);

            dirty = false;

            GL11.glPixelStorei(GL11.GL_UNPACK_ROW_LENGTH, 0);
            GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_PIXELS, 0);
            GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_ROWS, 0);
        } else {
            GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 4);

            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, width, height, 0, GL12.GL_BGRA,
                    GL12.GL_UNSIGNED_INT_8_8_8_8_REV, buffer);
        }
    }

    public int getPixel(int x, int y) {
        checkBounds(x, y);

        return pixelArray[y * width + x];
    }

    /**
     * The {@code setPixel} method does not respect the color's alpha value. The
     * pixels color is simply set to the new color.
     * 
     * @param x
     * @param y
     * @param color
     * @see Image#drawPixel
     */
    public void setPixel(int x, int y, int color) {
        checkBounds(x, y);

        pixelArray[y * width + x] = color;
        setDirty(x, y);
    }

    public void setPixels(int x, int y, int w, int h, int color) {
        checkBounds(x, y, w, h);

        for (int row = y; row < y + h; row++) {
            int fromIndex = row * width + x,
                    toIndex = fromIndex + w;
            Arrays.fill(pixelArray, fromIndex, toIndex, color);
        }

        setDirty(x, y);
        setDirty(x + w - 1, y + h - 1);
    }

    /**
     * The {@code drawPixel} method respects the color's alpha value. That means if
     * you draw with an alpha of 0.5, the pixel's color will become a 50/50 mix of
     * the old color and the new color.
     * 
     * @see Image#setPixel
     */
    public void drawPixel(int x, int y, int color) {
        checkBounds(x, y);

        drawPixelUnsafe(x, y, color);

        setDirty(x, y);
    }

    private void drawPixelUnsafe(int x, int y, int color) {
        int srcAlpha = SUtil.alpha(color);

        if (srcAlpha == 0)
            return;

        int c;
        if (srcAlpha == 255) {
            c = color;
        } else {
            int srcRed = SUtil.red(color),
                    srcGreen = SUtil.green(color),
                    srcBlue = SUtil.blue(color);

            int dst = getPixel(x, y);
            int dstRed = SUtil.red(dst),
                    dstGreen = SUtil.green(dst),
                    dstBlue = SUtil.green(dst),
                    dstAlpha = SUtil.alpha(dst);

            c = SUtil.toARGB(
                    ((255 - srcAlpha) * dstRed + srcAlpha * srcRed) / 255,
                    ((255 - srcAlpha) * dstGreen + srcAlpha * srcGreen) / 255,
                    ((255 - srcAlpha) * dstBlue + srcAlpha * srcBlue) / 255,
                    255 - (255 - srcAlpha) * (255 - dstAlpha) / 255);
        }
        pixelArray[y * width + x] = c;
    }

    public BufferedImage getSubImage(int x, int y, int w, int h, Integer backgroundColor) {
        checkBounds(x, y, w, h);

        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        int[] outPixels = ((DataBufferInt) out.getRaster().getDataBuffer()).getData();

        int srcStride = width,
                dstStride = w;

        if (backgroundColor == null || backgroundColor == 0) {
            for (int row = 0; row < h; row++) {
                System.arraycopy(pixelArray, (y + row) * srcStride + x, outPixels, row * dstStride, w);
            }
            return out;
        } else {
            for (int row = 0; row < h; row++) {
                for (int col = 0; col < w; col++) {
                    int c = pixelArray[(y + row) * srcStride + (x + col)];
                    outPixels[row * dstStride + col] = (c == backgroundColor) ? 0 : c;
                }
            }
            return out;
        }
    }

    /**
     * The {@code setSubImage} method does not respect the pixels' alpha values. The
     * pixels are simply copied over.
     * 
     * @see Image#drawSubImage(int, int, int, int, int[])
     */
    public void setSubImage(int x, int y, int w, int h, int[] pixels) {
        drawSubImage(x, y, w, h, pixels, false);
    }

    /**
     * The {@code drawSubImage} method respects the pixels' alpha values and dos the
     * appropriate alpha blending the the existing pixels.
     * 
     * @see Image#setSubImage(int, int, int, int, int[])
     */
    public void drawSubImage(int x, int y, int w, int h, int[] pixels) {
        drawSubImage(x, y, w, h, pixels, true);
    }

    private void drawSubImage(int x, int y, int w, int h, int[] pixels, boolean doAlphaBlending) {
        if (x >= width || x + w <= 0 || y >= height || y + h <= 0)
            return;

        int x0 = Math.max(0, x),
                y0 = Math.max(0, y),
                x1 = Math.min(x + w, width),
                y1 = Math.min(y + h, height);

        int stride = w;
        int offset = (y0 - y) * stride + (x0 - x);
        int len = x1 - x0;
        int numRows = y1 - y0;

        if (doAlphaBlending) {
            for (int row = 0; row < numRows; row++) {
                for (int col = 0; col < len; col++) {
                    int c = pixels[row * stride + offset + col];
                    drawPixelUnsafe(col + x0, row + y0, c);
                }
            }
        } else {
            // TODO continue: this doesn't work
            for (int row = 0; row < numRows; row++) {
                System.arraycopy(pixels, row * stride + offset, pixelArray, (y0 + row) * width + x0, len);
            }
        }

        setDirty(x0, y0);
        setDirty(x1 - 1, y1 - 1);
    }

    public void crop(int newWidth, int newHeight, int backgroundColor) {
        BufferedImage oldImage = bufferedImage,
                newImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);

        int oldWidth = oldImage.getWidth(),
                oldHeight = oldImage.getHeight();

        int[] oldPixels = ((DataBufferInt) oldImage.getRaster().getDataBuffer()).getData(),
                newPixels = ((DataBufferInt) newImage.getRaster().getDataBuffer()).getData();

        if (newWidth > oldWidth || newHeight > oldHeight)
            Arrays.fill(newPixels, backgroundColor);

        int copyWidth = Math.min(oldWidth, newWidth),
                copyHeight = Math.min(oldHeight, newHeight);

        for (int y = 0; y < newHeight; y++) {
            int x0 = 0;
            if (y < copyHeight) {
                System.arraycopy(oldPixels, y * oldWidth, newPixels, y * newWidth, copyWidth);
                x0 = copyWidth;
            }

            for (int x = x0; x < newWidth; x++) {
                newPixels[y * newWidth + x] = backgroundColor;
            }
        }

        bufferedImage = newImage;

        updateOpenGLTexture(false);
    }

    private void setDirty(int x, int y) {
        if (!dirty) {
            dirtyMinX = x;
            dirtyMaxX = x;
            dirtyMinY = y;
            dirtyMaxY = y;
        } else {
            dirtyMinX = Math.min(dirtyMinX, x);
            dirtyMaxX = Math.max(dirtyMaxX, x);
            dirtyMinY = Math.min(dirtyMinY, y);
            dirtyMaxY = Math.max(dirtyMaxY, y);
        }
        dirty = true;
    }

    public void cleanUp() {
        GL11.glDeleteTextures(textureID);
    }

    public BufferedImage getBufferedImage() {
        return bufferedImage;
    }

    public int getTextureID() {
        return textureID;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    private void checkBounds(int x, int y) {
        if (!isInside(x, y))
            throw new RuntimeException(String.format("Coordinate out of bounds: (%d, %d)", x, y));
    }

    private void checkBounds(int x, int y, int w, int h) {
        checkBounds(x, y);
        checkBounds(x + w - 1, y + h - 1);
    }

    public boolean isInside(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }
}