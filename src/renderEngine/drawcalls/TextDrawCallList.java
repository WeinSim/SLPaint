package renderEngine.drawcalls;

public class TextDrawCallList extends DrawCallList<TextDrawCall, TextData, TextVAO> {

    public TextDrawCallList() {
        super();
    }

    @Override
    protected TextVAO createNewVAO() {
        return new TextVAO();
    }
}