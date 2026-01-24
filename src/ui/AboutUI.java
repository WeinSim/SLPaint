package ui;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.function.Supplier;

import main.apps.AboutApp;
import sutil.ui.UIContainer;
import sutil.ui.UIText;

public class AboutUI extends AppUI<AboutApp> {

    private static final String ABOUT_TEXT_FILE = "res/about.txt";

    public AboutUI(AboutApp app) {
        super(app);
    }

    @Override
    protected void init() {
        root.setMinimalSize();
        root.setMarginScale(2.0);
        root.setPaddingScale(0.5);

        root.setAlignment(UIContainer.LEFT);

        // root.add(new UILabel("SLPaint"));

        // UIContainer textContainer = new UIContainer(UIContainer.VERTICAL,
        // UIContainer.LEFT);
        // textContainer.noOutline();
        String aboutText = null;
        try (BufferedReader reader = new BufferedReader(new FileReader(ABOUT_TEXT_FILE))) {
            aboutText = reader.readAllAsString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (aboutText == null)
            aboutText = "[unable to load about]";

        for (String line : aboutText.split("\n")) {
            int alignment = UIContainer.LEFT;
            Supplier<Double> textSize = UIText.NORMAL;
            if (line.startsWith("<")) {
                char[] charArray = line.toCharArray();
                int endIndex = -1;
                for (int i = 0; i < charArray.length; i++) {
                    switch (charArray[i]) {
                        case 'c' -> alignment = UIContainer.CENTER;
                        case 's' -> textSize = UIText.SMALL;
                        case '>' -> {
                            endIndex = i;
                            i = charArray.length;
                        }
                    }
                }
                line = line.substring(endIndex + 1);
            }
            UIContainer textContainer = new UIContainer(UIContainer.VERTICAL, alignment);
            textContainer.zeroMargin().setHFillSize().noOutline();
            textContainer.add(new UIText(line, textSize));
            root.add(textContainer);
        }

        // textContainer.add(new UILabel(aboutText).setPaddingScale(0.5));

        // root.add(textContainer);

        // root.add(new UILabel("\u00A9 Simon Weinzierl, 2026", UIText.SMALL));
    }
}