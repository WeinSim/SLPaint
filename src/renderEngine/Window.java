package renderEngine;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCharCallbackI;
import org.lwjgl.glfw.GLFWKeyCallbackI;
import org.lwjgl.glfw.GLFWMouseButtonCallbackI;
import org.lwjgl.glfw.GLFWScrollCallbackI;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import sutil.math.SVector;

public class Window {

    public static final int NORMAL = 0, MAXIMIZED = 1, FULLSCREEN = 2;

    private static HashMap<Long, Window> windows = new HashMap<>();

    private long windowHandle;

    private static long arrowCursor;
    private static long handCursor;
    private static long iBeamCursor;

    private SVector mousePos;

    private String charBuffer;
    private ArrayList<KeyPressInfo> keyPressInfos;
    private ArrayList<MouseButtonInfo> mouseButtonInfos;
    private ArrayList<ScrollInfo> scrollInfos;

    public Window(int width, int height, int windowMode, boolean resizable, String title) {
        // Configure GLFW
        GLFW.glfwDefaultWindowHints(); // optional, the current window hints are already the default
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE); // the window will stay hidden after creation
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, resizable ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE);

        // anti-aliasing
        // GLFW.glfwWindowHint(GLFW.GLFW_SAMPLES, 4);

        if (windowMode == MAXIMIZED) {
            GLFW.glfwWindowHint(GLFW.GLFW_MAXIMIZED, GLFW.GLFW_TRUE);
        }

        // Create the window
        // window = GLFW.glfwCreateWindow(width, height, "Hello World!",
        // MemoryUtil.NULL, MemoryUtil.NULL);
        long primaryMonitor = GLFW.glfwGetPrimaryMonitor();
        GLFWVidMode pmVideoMode = GLFW.glfwGetVideoMode(primaryMonitor);
        if (windowMode == FULLSCREEN) {
            width = pmVideoMode.width();
            height = pmVideoMode.height();
        }
        windowHandle = GLFW.glfwCreateWindow(width, height, title,
                windowMode == FULLSCREEN ? primaryMonitor : MemoryUtil.NULL,
                MemoryUtil.NULL);
        windows.put(windowHandle, this);
        if (windowHandle == MemoryUtil.NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        // Setup a key callback. It will be called every time a key is pressed, repeated
        // or released.
        GLFW.glfwSetCharCallback(windowHandle, new CharCallback());
        GLFW.glfwSetKeyCallback(windowHandle, new KeyCallback());
        GLFW.glfwSetMouseButtonCallback(windowHandle, new MouseButtonCallback());
        GLFW.glfwSetScrollCallback(windowHandle, new ScrollCallback());

        center();

        // Make the OpenGL context current
        makeContextCurrent();
        // Enable v-sync.
        // Note: this limits the application to 60fps, which can make very fast paint
        // strokes look jagged. To improve this, the rendering logic could be separated
        // onto its own thread. For now though, this is not neccessary.
        GLFW.glfwSwapInterval(1);

        // GLFW.glfwSetWindowSizeLimits(windowHandle, 200, 200, Integer.MAX_VALUE,
        // Integer.MAX_VALUE);
        GLFW.glfwSetWindowSizeLimits(windowHandle, 200, 200, GLFW.GLFW_DONT_CARE, GLFW.GLFW_DONT_CARE);

        arrowCursor = GLFW.glfwCreateStandardCursor(GLFW.GLFW_ARROW_CURSOR);
        handCursor = GLFW.glfwCreateStandardCursor(GLFW.GLFW_HAND_CURSOR);
        iBeamCursor = GLFW.glfwCreateStandardCursor(GLFW.GLFW_IBEAM_CURSOR);

        // Make the window visible
        GLFW.glfwShowWindow(windowHandle);

        GL.createCapabilities();

        mousePos = new SVector();

        charBuffer = "";
        keyPressInfos = new ArrayList<>();
        mouseButtonInfos = new ArrayList<>();
        scrollInfos = new ArrayList<>();
    }

    public void makeContextCurrent() {
        GLFW.glfwMakeContextCurrent(windowHandle);
    }

    public void updateDisplay() {
        mousePos = null;
        charBuffer = "";

        GLFW.glfwSwapBuffers(windowHandle);
    }

    public boolean isCloseRequested() {
        return GLFW.glfwWindowShouldClose(windowHandle);
    }

    public void requestClose() {
        GLFW.glfwSetWindowShouldClose(windowHandle, true); // We will detect this in the rendering loop
    }

    public void closeDisplay() {
        // Free the window callbacks and destroy the window
        Callbacks.glfwFreeCallbacks(windowHandle);
        GLFW.glfwDestroyWindow(windowHandle);
    }

    public void hideCursor() {
        GLFW.glfwSetInputMode(windowHandle, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
    }

    public int[] getDisplaySize() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            GLFW.glfwGetWindowSize(windowHandle, pWidth, pHeight);

            return new int[] { pWidth.get(0), pHeight.get(0) };
        }
    }

    public float[] getWindowContentScale() {
        float[] xScale = new float[1],
                yScale = new float[1];
        GLFW.glfwGetWindowContentScale(windowHandle, xScale, yScale);

        // System.out.format("GLFW window content scale: x = %.1f, y = %.1f\n",
        // xScale[0], yScale[0]);

        return new float[] { xScale[0], yScale[0] };
    }

    public long getWindowHandle() {
        return windowHandle;
    }

    public SVector getMousePosition() {
        if (mousePos == null) {
            double[] x = { 0 };
            double[] y = { 0 };
            GLFW.glfwGetCursorPos(windowHandle, x, y);
            mousePos = new SVector(x[0], y[0]);
        }
        return mousePos;
    }

    public void showCursor() {
        GLFW.glfwSetInputMode(windowHandle, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
    }

    public void setArrowCursor() {
        GLFW.glfwSetCursor(windowHandle, arrowCursor);
    }

    public void setHandCursor() {
        GLFW.glfwSetCursor(windowHandle, handCursor);
    }

    public void setIBeamCursor() {
        GLFW.glfwSetCursor(windowHandle, iBeamCursor);
    }

    public boolean isFocused() {
        return GLFW.glfwGetWindowAttrib(windowHandle, GLFW.GLFW_FOCUSED) == GLFW.GLFW_TRUE;
    }

    public void setSizeAndCenter(int width, int height) {
        setSize(width, height);

        center(width, height);
    }

    public void setSize(int width, int height) {
        GLFW.glfwSetWindowSize(windowHandle, width, height);
    }

    public void center() {
        int[] displaySize = getDisplaySize();
        center(displaySize[0], displaySize[1]);
    }

    private void center(int width, int height) {
        long primaryMonitor = GLFW.glfwGetPrimaryMonitor();
        GLFWVidMode pmVideoMode = GLFW.glfwGetVideoMode(primaryMonitor);
        PointerBuffer monitors = GLFW.glfwGetMonitors();
        int offset = monitors.capacity() == 2 ? 1920 : 0;
        offset = 0;
        GLFW.glfwSetWindowPos(windowHandle,
                offset + (pmVideoMode.width() - width) / 2,
                (pmVideoMode.height() - height) / 2);
    }

    public void requestFocus() {
        GLFW.glfwFocusWindow(windowHandle);
    }

    private void addCharacter(int codepoint) {
        charBuffer += (char) codepoint;
    }

    public Character getNextCharacter() {
        if (charBuffer.isEmpty()) {
            return null;
        }
        char c = charBuffer.charAt(0);
        charBuffer = charBuffer.substring(1);
        return c;
    }

    private void addKeyPressInfo(KeyPressInfo keyPressInfo) {
        keyPressInfos.add(keyPressInfo);
    }

    public KeyPressInfo getNextKeyPressInfo() {
        if (keyPressInfos.isEmpty()) {
            return null;
        }
        return keyPressInfos.removeFirst();
    }

    private void addMouseButtonInfo(MouseButtonInfo mouseButtonInfo) {
        mouseButtonInfos.add(mouseButtonInfo);
    }

    public MouseButtonInfo getNextMouseButtonInfo() {
        if (mouseButtonInfos.isEmpty()) {
            return null;
        }
        return mouseButtonInfos.removeFirst();
    }

    private void addScrollInfo(ScrollInfo scrollInfo) {
        scrollInfos.add(scrollInfo);
    }

    public ScrollInfo getNextScrollInfo() {
        if (scrollInfos.isEmpty()) {
            return null;
        }
        return scrollInfos.removeFirst();
    }

    private static class CharCallback implements GLFWCharCallbackI {

        @Override
        public void invoke(long window, int codepoint) {
            windows.get(window).addCharacter(codepoint);
        }
    }

    private static class KeyCallback implements GLFWKeyCallbackI {

        @Override
        public void invoke(long window, int key, int scancode, int action, int mods) {
            windows.get(window).addKeyPressInfo(new KeyPressInfo(key, scancode, action, mods));
        }
    }

    public record KeyPressInfo(int key, int scancode, int action, int mods) {
    }

    private static class MouseButtonCallback implements GLFWMouseButtonCallbackI {

        @Override
        public void invoke(long window, int button, int action, int mods) {
            windows.get(window).addMouseButtonInfo(new MouseButtonInfo(button, action, mods));
        }
    }

    public record MouseButtonInfo(int button, int action, int mods) {
    }

    private static class ScrollCallback implements GLFWScrollCallbackI {

        @Override
        public void invoke(long window, double xoffset, double yoffset) {
            windows.get(window).addScrollInfo(new ScrollInfo(xoffset, yoffset));
        }
    }

    public record ScrollInfo(double xoffset, double yoffset) {
    }
}