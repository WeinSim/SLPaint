package main;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

public enum ImageFormat {

    PNG("png"),
    JPG("JPEG Files", new String[] { "jpg", "jpeg" }),
    WEBP("webp"),
    BMP("bmp"),
    GIF("gif");

    public final String name;
    public final String[] extensions;
    public final FileNameExtensionFilter fileFilter;

    private ImageFormat(String extension) {
        this("%s Files".formatted(extension.toUpperCase()), new String[] { extension });
    }

    private ImageFormat(String name, String[] extensions) {
        this.name = name;
        this.extensions = extensions;

        String description = name + " (";
        for (int i = 0; i < extensions.length; i++) {
            String extension = extensions[i];
            description += "*.%s".formatted(extension);
            if (i < extensions.length - 1) {
                description += ", ";
            } else {
                description += ")";
            }
        }
        fileFilter = new FileNameExtensionFilter(description, extensions);
    }

    public static ImageFormat fromFileFilter(FileFilter filter) {
        for (ImageFormat format : values()) {
            if (filter == format.fileFilter) {
                return format;
            }
        }
        return null;
    }

    public static ImageFormat fromString(String formatName) {
        formatName = formatName.toLowerCase();
        for (ImageFormat format : values()) {
            for (String extension : format.extensions) {
                if (formatName.equals(extension)) {
                    return format;
                }
            }
        }
        return null;
    }
}