package main.tools;

public interface Resizable {

    public void startResizing();

    public void finishResizing();

    public boolean lockRatio();

    public int getX();

    public void setX(int x);

    public int getY();

    public void setY(int y);

    public int getWidth();

    public void setWidth(int width);

    public int getHeight();

    public void setHeight(int height);
}