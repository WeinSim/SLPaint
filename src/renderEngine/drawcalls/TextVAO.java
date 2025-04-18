package renderEngine.drawcalls;

public class TextVAO extends DrawVAO<TextDrawCall, TextData> {

    public TextVAO() {
        super();
    }

    @Override
    public int getVertexCount(TextDrawCall drawCall) {
        return drawCall.getText().length();
    }
}