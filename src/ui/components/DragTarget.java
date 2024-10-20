package ui.components;

import sutil.math.SVector;

public interface DragTarget {

    public void drag();

    public void setCursorPosition(SVector position);
    
}