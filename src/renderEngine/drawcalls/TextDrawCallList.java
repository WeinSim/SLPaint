package renderEngine.drawcalls;

public class TextDrawCallList extends DrawCallList<TextDrawCall, TextData> {

    public TextDrawCallList() {
        super(c -> c.getText().length());
    }
}