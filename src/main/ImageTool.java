package main;

import java.util.LinkedList;

import main.apps.MainApp;

public enum ImageTool {

    PENCIL, COLOR_PICKER, FILL_BUCKET, SELECTION;

    private static final int[] FILL_XOFF = { 0, -1, 0, 1 };
    private static final int[] FILL_YOFF = { 1, 0, -1, 0 };

    public void click(MainApp app, int x, int y, int mouseButton) {
        switch (this) {
            case COLOR_PICKER -> {
                switch (mouseButton) {
                    case 0 -> app.setPrimaryColor(app.getImage().getPixel(x, y));
                    case 1 -> app.setSecondaryColor(app.getImage().getPixel(x, y));
                }
            }
            case FILL_BUCKET -> {
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
                            if (image.getPixel(newX, newY)  == baseColor) {
                                boundary.add((newX & 0xFFFFFFFFL) << 32 | (newY & 0xFFFFFFFFL));
                            }
                        }
                    }
                }
            }
            default -> {
            }
        }
    }

    public void update(MainApp app, int x, int y, int mouseButton) {
        switch (this) {
            case PENCIL -> {
                app.drawLine(x, y, mouseButton == 0 ? app.getPrimaryColor() : app.getSecondaryColor());
            }
            default -> {
            }
        }
    }

    public void release(MainApp app, int x, int y, int mouseButton) {
        switch (this) {
            default -> {
            }
        }
    }
}