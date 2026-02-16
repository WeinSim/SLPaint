package sutil.ui.elements;

import java.util.concurrent.CompletableFuture;
import java.util.function.DoubleSupplier;

import org.lwjglx.util.vector.Vector4f;

import sutil.ui.UI;
import sutil.ui.UISizes;

public class UIModalDialog extends UIFloatContainer {

    private CompletableFuture<Integer> fututre;
    private boolean finished;

    public UIModalDialog(String title, String message, int dialogType, CompletableFuture<Integer> future) {
        super(VERTICAL, CENTER);

        this.fututre = future;
        finished = false;

        // A UIModalDialog automatically gets removed from its parent (the root) when it
        // turns invisible.
        setVisibilitySupplier(() -> !finished);

        style.setBackgroundColor(new Vector4f(0.0f, 0.0f, 0.0f, 0.5f));
        outlineNormal = false;

        UIContainer content = new UIContainer(VERTICAL, CENTER);
        content.zeroPadding().zeroMargin().withBackground();
        // content.setFixedSize(UISizes.DIALOG.getWidthHeight());

        UIContainer topRow = new UIContainer(HORIZONTAL, RIGHT, CENTER);
        topRow.setHFillSize().zeroMargin();
        topRow.add(new UILabel(title).setHFillSize().setHAlignment(CENTER));
        topRow.add(new UIButton("X", () -> finish(UI.CLOSED_OPTION)));
        content.add(topRow);

        UIContainer mainArea = new UIContainer(VERTICAL, CENTER);
        mainArea.setMarginScale(UISizes.DIALOG_MARGIN.get() / UISizes.MARGIN.get());
        mainArea.setPaddingScale(0.5).setMinimalSize().noOutline();
        for (String line : message.split("\n")) {
            int alignment = LEFT;
            DoubleSupplier textSize = UIText.NORMAL;
            if (line.startsWith("<")) {
                char[] charArray = line.toCharArray();
                int endIndex = -1;
                for (int i = 0; i < charArray.length; i++) {
                    switch (charArray[i]) {
                        case 'c' -> alignment = CENTER;
                        case 's' -> textSize = UIText.SMALL;
                        case '>' -> {
                            endIndex = i;
                            i = charArray.length;
                        }
                    }
                }
                line = line.substring(endIndex + 1);
            }
            UIContainer textContainer = new UIContainer(VERTICAL, alignment);
            textContainer.zeroMargin().setHFillSize().noOutline();
            textContainer.add(new UIText(line, textSize));
            mainArea.add(textContainer);
        }
        content.add(mainArea);

        UIContainer bottomRow = new UIContainer(HORIZONTAL, RIGHT, CENTER);
        bottomRow.setHFillSize();
        bottomRow.noOutline();
        String[] buttonLabels;
        int[] returnCodes;
        switch (dialogType) {
            case UI.YES_NO_DIALOG -> {
                buttonLabels = new String[] { "No", "Yes" };
                returnCodes = new int[] { UI.NO_OPTION, UI.YES_OPTION };
            }
            case UI.OK_CANCEL_DIALOG -> {
                buttonLabels = new String[] { "Cancel", "Ok" };
                returnCodes = new int[] { UI.NO_OPTION, UI.YES_OPTION };
            }
            case UI.YES_NO_CANCEL_DIALOG -> {
                buttonLabels = new String[] { "Cancel", "No", "Yes" };
                returnCodes = new int[] { UI.CANCEL_OPTION, UI.NO_OPTION, UI.YES_OPTION };
            }
            case UI.INFO_DIALOG -> {
                buttonLabels = new String[] { "Ok" };
                returnCodes = new int[] { UI.OK_OPTION };
            }
            default -> throw new RuntimeException(String.format("Invalid UI modal dialog type: %d", dialogType));
        }
        for (int i = 0; i < returnCodes.length; i++) {
            final int j = i;
            UIButton button = new UIButton(buttonLabels[i], () -> finish(returnCodes[j]));
            button.setHFillSize();
            bottomRow.add(button);
        }
        content.add(bottomRow);

        add(content);
    }

    @Override
    public void update() {
        super.update();

        setFixedSize(UI.getRootSize());

        relativeLayer = UI.MODAL_DIALOG_LAYER - parent.relativeLayer;
    }

    private void finish(int returnCode) {
        fututre.complete(returnCode);
        finished = true;
        UI.queueEvent(() -> parent.remove(this));
    }
}