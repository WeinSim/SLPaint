package main.tools;

public abstract sealed class ClickTool extends ImageTool permits PipetteTool, FillBucketTool {

    public ClickTool() {
    }

    @Override
    protected final boolean startInitialDrag(int x, int y, int mouseButton) {
        click(x, y, mouseButton);
        return false;
    }

    protected abstract void click(int x, int y, int mouseButton);

    @Override
    protected void handleInitialDrag(int x, int y, int px, int py) {
        invalidState();
    }

    @Override
    protected boolean finishInitialDrag() {
        invalidState();
        return false;
    }

    @Override
    protected boolean startIdleDrag(int x, int y, int mouseButton) {
        invalidState();
        return false;
    }

    @Override
    protected void handleIdleDrag(int x, int y, int px, int py) {
        invalidState();
    }

    @Override
    protected void finishIdleDrag() {
        invalidState();
    }

    @Override
    public void forceQuit() {
        // nothing to do
    }
    
    private void invalidState() {
        final String baseString = "INITIAL_DRAG, IDLE and IDLE_DRAG states undefined for %s tool";
        throw new UnsupportedOperationException(String.format(baseString, getName()));
    }
}