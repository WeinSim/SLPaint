package main.tools;

import java.util.LinkedList;

import main.Image;

public final class FillBucketTool extends ClickTool {

    public static final FillBucketTool INSTANCE = new FillBucketTool();

    private static final int[] FILL_XOFF = { 0, -1, 0, 1 };
    private static final int[] FILL_YOFF = { 1, 0, -1, 0 };

    private FillBucketTool() {
    }

    @Override
    protected void click(int x, int y, int mouseButton) {
        Image image = app.getImage();
        int baseColor = image.getPixel(x, y);
        int replaceColor = mouseButton == 0 ? app.getPrimaryColor() : app.getSecondaryColor();
        // int bitmask = MainApp.RGB_BITMASK;
        // if ((baseColor & bitmask) == (replaceColor & bitmask))
        // // break;
        // return;
        LinkedList<Long> boundary = new LinkedList<>();
        boundary.add((x & 0xFFFFFFFFL) << 32 | (y & 0xFFFFFFFFL));
        // no noticeable performance increase with cached bitmap for discovered pixels
        // boolean[][] discovered = new boolean[image.getWidth()][image.getHeight()];
        while (!boundary.isEmpty()) {
            long point = boundary.removeLast();
            int pointX = (int) ((point >> 32) & 0xFFFFFFFFL);
            int pointY = (int) (point & 0xFFFFFFFFL);
            // discovered[pointX][pointY] = true;
            image.setPixel(pointX, pointY, replaceColor);
            for (int i = 0; i < FILL_XOFF.length; i++) {
                int newX = pointX + FILL_XOFF[i];
                int newY = pointY + FILL_YOFF[i];
                if (image.isInside(newX, newY)) {
                    // if (discovered[newX][newY]) {
                    // continue;
                    // }
                    if (image.getPixel(newX, newY) == baseColor) {
                        boundary.add((newX & 0xFFFFFFFFL) << 32 | (newY & 0xFFFFFFFFL));
                    }
                }
            }
        }
    }

    @Override
    public String getName() {
        return "Fill Bucket";
    }
}