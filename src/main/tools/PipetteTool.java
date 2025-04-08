package main.tools;

public final class PipetteTool extends ClickTool {

    public static final PipetteTool INSTANCE = new PipetteTool();

    private PipetteTool() {
    }

    @Override
    protected void click(int x, int y, int mouseButton) {
        switch (mouseButton) {
            case 0 -> app.setPrimaryColor(app.getImage().getPixel(x, y));
            case 1 -> app.setSecondaryColor(app.getImage().getPixel(x, y));
            default -> {
                return;
            }
        }
        app.switchBackToPreviousTool();
    }

    @Override
    public String getName() {
        return "Pipette";
    }
}