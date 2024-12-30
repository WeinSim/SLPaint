package main;

import java.util.ArrayList;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;

import main.apps.App;
import main.apps.MainApp;
import renderEngine.Window;
import sutil.SUtil;

public class MainLoop {

    private static ArrayList<App> apps;

    public static void main(String[] args) {
        SUtil.printNumLines();

        initGLFW();

        apps = new ArrayList<>();

        MainApp mainApp = new MainApp();
        Window mainWindow = mainApp.getWindow();

        double deltaT = 1.0 / 60.0;
        while (!mainWindow.isCloseRequested()) {
            long startTime = System.nanoTime();

            for (int i = apps.size() - 1; i >= 0; i--) {
                App app = apps.get(i);
                GLFW.glfwMakeContextCurrent(app.getWindow().getWindowHandle());

                app.update(deltaT);
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