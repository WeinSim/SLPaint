package main;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

public class ImageFile {

    private final Image image;
    private final File file;
    private final ImageFormat format;
    // private long size;

    private final RandomAccessFile raFile;
    private final FileLock lock;
    private final FileChannel channel;

    /**
     * Creates a new {@code ImageFile} by loading the image from the specified file.
     * 
     * @param file
     * @throws IOException
     */
    public ImageFile(File file) throws IOException {
        // 1st step: lock the file
        this.file = file;
        // size = file.length();
        raFile = new RandomAccessFile(file, "rw");
        channel = raFile.getChannel();
        // pass true as 3rd argument because other files should still be able to read
        // the file's content
        lock = channel.lock(0, Long.MAX_VALUE, true);

        BufferedImage bufferedimage = null;
        FileInputStream fis = new FileInputStream(file);
        try (ImageInputStream iis = ImageIO.createImageInputStream(fis)) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            String formatName = null;
            if (readers.hasNext()) {
                ImageReader reader = readers.next();
                formatName = reader.getFormatName();
                format = ImageFormat.fromString(formatName);
                if (format == null) {
                    throw new IOException(String.format(
                            "Unknown image format: \"%s\"! "
                                    + "This format is not known by the interface main.ImageFormat.\n",
                            formatName));
                }
                reader.setInput(iis);
                bufferedimage = reader.read(0);
            } else {
                throw new IOException("No fitting ImageReader was found for the given file!");
            }
        }
        image = new Image(bufferedimage);
    }

    /**
     * Creates a new {@code ImageFile} using the given image, file and format.
     * This constructor saves the image to the file using the given format.
     * 
     * @param image
     * @param file
     * @param format
     * @throws IOException
     */
    public ImageFile(Image image, File file, ImageFormat format) throws IOException {
        this.image = image;
        this.format = format;
        String extension = "." + format.extensions[0];
        if (!file.getName().endsWith(extension)) {
            this.file = new File(file.getAbsolutePath() + extension);
        } else {
            this.file = file;
        }
        if (!save()) {
            throw new IOException();
        }

        raFile = new RandomAccessFile(this.file, "rw");
        channel = raFile.getChannel();
        // pass true as 3rd argument because other files should still be able to read
        // the file's content
        lock = channel.lock(0, Long.MAX_VALUE, true);
        // size = file.length();
    }

    public boolean save() throws IOException {
        boolean returnValue = ImageIO.write(image.getBufferedImage(), format.extensions[0], file);
        // size = file.length();
        return returnValue;
    }

    public void close() {
        try {
            if (lock != null) {
                lock.release();
            }
            if (channel != null) {
                channel.close();
            }
            if (raFile != null) {
                raFile.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Image getImage() {
        return image;
    }

    public File getFile() {
        return file;
    }

    public long getSize() {
        // return size;
        return file.length();
    }

    public ImageFormat getFormat() {
        return format;
    }
}