package main;

public class ColorButtonArray {

    private Integer[] colors;

    public ColorButtonArray(int length) {
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