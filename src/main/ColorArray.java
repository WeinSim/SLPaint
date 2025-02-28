package main;

public class ColorArray {

    private Integer[] colors;
    private int length;

    public ColorArray(int length) {
        this.length = length;
        clear();
    }

    public void clear() {
        colors = new Integer[length];
    }

    public void addColor(int color) {
        for (int i = colors.length - 1; i >= 1; i--) {
            colors[i] = colors[i - 1];
        }
        colors[0] = color;
    }

    public Integer getColor(int index) {
        return colors[index];
    }

    public int getLength() {
        return colors.length;
    }
}