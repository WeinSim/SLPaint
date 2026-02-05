package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.newdawn.slick.util.Log;

import main.apps.App;
import main.apps.MainApp;
import renderengine.Window;
import sutil.SUtil;

public class MainLoop {

    private static ArrayList<App> apps;

    public static void main(String[] args) {
        long programStartTime = System.nanoTime();
        boolean firstLoop = true;

        SUtil.printNumLines();

        testImports();

        // Loader.createFontAtlas("Courier New", 36);
        // Loader.createFontAtlas("Courier New Bold", 18);

        // Disables the message
        // "INFO:Use Java PNG Loader = true"
        // that would otherwise show up when loading the first image
        Log.setVerbose(false);

        initGLFW();

        apps = new ArrayList<>();
        MainApp mainApp = new MainApp();
        Window mainWindow = mainApp.getWindow();

        double deltaT = 1.0 / 60.0;
        while (!mainWindow.isCloseRequested()) {
            long startTime = System.nanoTime();

            for (int i = apps.size() - 1; i >= 0; i--) {
                App app = apps.get(i);

                // For some reason, with the new rendering system I have to call
                // makeContextCurrent() twice. I have absolutely no idea why.
                app.makeContextCurrent();
                app.update(deltaT);
                app.makeContextCurrent();
                app.render();

                Window window = app.getWindow();
                window.updateDisplay();

                if (app != mainApp) {
                    if (window.isCloseRequested()) {
                        app.finish();
                        window.closeDisplay();
                        apps.remove(i);
                    }
                }
            }

            GLFW.glfwPollEvents();

            if (firstLoop) {
                long duration = System.nanoTime() - programStartTime;
                System.out.format("Startup time: %.1fms\n", duration * 1e-6);
                firstLoop = false;
            }

            long duration = System.nanoTime() - startTime;
            deltaT = 1e-9 * duration;
        }

        for (App app : apps) {
            app.finish();
            app.getWindow().closeDisplay();
        }

        terminateGLFW();
    }

    public static void addApp(App app) {
        apps.add(app);
    }

    private static void initGLFW() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if (!GLFW.glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");
    }

    private static void terminateGLFW() {
        // Terminate GLFW and free the error callback
        GLFW.glfwTerminate();
        GLFW.glfwSetErrorCallback(null).free();
    }

    /**
     * checks that all sutil imports are self contained
     */
    private static void testImports() {
        testImports(new File("src/sutil"), new String[] { "sutil", "java", "org" });
    }

    private static void testImports(File file, String[] allowedImports) {
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                testImports(child, allowedImports);
            }
        } else {
            if (!file.getName().endsWith(".java")) {
                return;
            }

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line = reader.readLine();
                for (int lineNumber = 1; line != null; line = reader.readLine(), lineNumber++) {
                    if (!line.startsWith("import ")) {
                        continue;
                    }
                    final int startIndex = "import ".length();
                    int endIndex = line.indexOf('.');
                    String importName = line.substring(startIndex, endIndex);
                    boolean allowed = false;
                    for (String allowedImport : allowedImports) {
                        if (allowedImport.equals(importName)) {
                            allowed = true;
                            break;
                        }
                    }
                    if (allowed) {
                        continue;
                    }
                    System.out.format("Illegal import in %s:%d:  %s\n", file.getPath(), lineNumber, line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // private static void textFilesizes() {
    // Random random = new Random();
    // for (int i = 0; i < 1000; i++) {
    // long l = Math.abs(random.nextInt());
    // System.out.format("%010d %s\n", l, MainApp.formatFilesize(l));
    // }
    // }

    // private static void testImages() {
    // // try {
    // // BufferedImage img = ImageIO.read(new File("smiley.png"));
    // // System.out.println(img.getRGB(0, 100));
    // // String[] fileExtensions = ImageIO.getWriterFileSuffixes();
    // // for (String extension : fileExtensions) {
    // // boolean ret = ImageIO.write(img, extension, new File("output." +
    // extension));
    // // System.out.format(".%s\t%b\n", extension, ret);
    // // }
    // // } catch (IOException e) {
    // // e.printStackTrace();
    // // }
    // for (String suffix : ImageIO.getReaderFileSuffixes()) {
    // check(suffix);
    // }
    // try {
    // System.out.println(Class.forName("com.sun.imageio.plugins.jpeg.JPEGImageReader"));
    // } catch (ClassNotFoundException e) {
    // e.printStackTrace();
    // }

    // }

    // private static void check(String suffix) {
    // Iterator<ImageReader> readers = ImageIO.getImageReadersBySuffix(suffix);
    // int i = 0;
    // while (readers.hasNext()) {
    // ImageReader reader = readers.next();
    // System.out.printf("%s: %d: %s%n", suffix, i++, reader);
    // }
    // }
}