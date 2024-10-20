package main;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import sutil.SUtil;

public class Image {

    private int textureID;
    private BufferedImage bufferedImage;

    private boolean dirty;
    private int dirtyMinX;
    private int dirtyMaxX;
    private int dirtyMinY;
    private int dirtyMaxY;

    public Image(BufferedImage bufferedImage) {
        init(bufferedImage);
    }

    private void init(BufferedImage image) {
        bufferedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        bufferedImage.getGraphics().drawImage(image, 0, 0, null);

        textureID = GL11.glGenTextures();

        dirtyMinX = 0;
        dirtyMaxX = bufferedImage.getWidth() - 1;
        dirtyMinY = 0;
        dirtyMaxY = bufferedImage.getHeight() - 1;
        updateOpenGLTexture(true);
    }

    public void updateOpenGLTexture() {
        if (dirty) {
            updateOpenGLTexture(false);
        }
    }

    private void updateOpenGLTexture(boolean firstTime) {
        int width = dirtyMaxX - dirtyMinX + 1;
        int height = dirtyMaxY - dirtyMinY + 1;
        int[] pixels = bufferedImage.getRGB(dirtyMinX, dirtyMinY, width, height, null, 0,
                bufferedImage.getWidth());

        ByteBuffer buffer = ByteBuffer.allocateDirect(width * height * 4);
        buffer.order(ByteOrder.nativeOrder());

        for (int y = dirtyMinY; y <= dirtyMaxY; y++) {
            for (int x = dirtyMinX; x <= dirtyMaxX; x++) {
                int pixel = pixels[(y - dirtyMinY) * getWidth() + (x - dirtyMinX)];
                buffer.put((byte) ((pixel >> 16) & 0xFF));
                buffer.put((byte) ((pixel >> 8) & 0xFF));
                buffer.put((byte) (pixel & 0xFF));
                buffer.put((byte) 0xFF);
            }
        }

        buffer.flip();

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
        if (firstTime) {
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, bufferedImage.getWidth(), bufferedImage.getHeight(),
                    0, GL12.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
        } else {
            GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, dirtyMinX, dirtyMinY, width, height, GL11.GL_RGBA,
                    GL11.GL_UNSIGNED_BYTE, buffer);
        }

        dirty = false;
    }

    public BufferedImage getSubImage(int startX, int startY, int width, int height) {
        int[] oldPixels = bufferedImage.getRGB(startX, startY, width, height, null, 0, getWidth());
        int[] newPixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                newPixels[y * width + x] = oldPixels[y * getWidth() + x];
            }
        }
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        image.setRGB(0, 0, width, height, newPixels, 0, width);
        return image;
    }

    public int getPixel(int x, int y) {
        int[] array = new int[3];
        bufferedImage.getRaster().getPixel(x, y, array);
        return SUtil.toARGB(array[0], array[1], array[2]);
    }

    public void setSubImage(BufferedImage image, int x, int y) {
        int x0 = Math.max(0, x);
        int y0 = Math.max(0, y);
        int x1 = Math.min(x + image.getWidth(), getWidth());
        int y1 = Math.min(y + image.getHeight(), getHeight());
        int w = x1 - x0;
        int h = y1 - y0;
        if (w <= 0 || h <= 0) {
            return;
        }
        int[] pixels = new int[3 * w * h];
        int index = 0;
        for (int yoff = 0; yoff < h; yoff++) {
            for (int xoff = 0; xoff < w; xoff++) {
                int color = image.getRGB(xoff - x + x0, yoff - y + y0);
                pixels[index++] = SUtil.red(color);
                pixels[index++] = SUtil.green(color);
                pixels[index++] = SUtil.blue(color);
            }
        }

        bufferedImage.getRaster().setPixels(x0, y0, w, h, pixels);

        setDirty(x0, y0);
        setDirty(x0 + w - 1, y0 + h - 1);
    }

    public void setPixel(int x, int y, int color) {
        if (!isInside(x, y)) {
            return;
        }
        int[] colorArray = {
                SUtil.red(color),
                SUtil.green(color),
                SUtil.blue(color)
        };
        bufferedImage.getRaster().setPixel(x, y, colorArray);
        setDirty(x, y);
    }

    public void setPixels(int x, int y, int width, int height, int color) {
        if (x < 0 || x + width > getWidth() || y < 0 || y + height > getHeight()) {
            return;
        }
        int[] array = new int[3 * width * height];
        int r = SUtil.red(color);
        int g = SUtil.green(color);
        int b = SUtil.blue(color);
        int[] colorArray = { r, g, b };
        for (int i = 0; i < array.length; i++) {
            array[i] = colorArray[i % colorArray.length];
        }
        bufferedImage.getRaster().setPixels(x, y, width, height, array);

        setDirty(x, y);
        setDirty(x + width - 1, y + height - 1);
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
        return bufferedImage.getWidth();
    }

    public int getHeight() {
        return bufferedImage.getHeight();
    }

    public boolean isInside(int x, int y) {
        return x >= 0 && x < getWidth() && y >= 0 && y < getHeight();
    }
}