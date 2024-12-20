package pv.opengl.window;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLUtil;
import org.lwjgl.system.MemoryUtil;
import pv.util.logging.PVLogger;

public class WindowFactory {
    public static long createWindow(int width, int height, String title, PVLogger logger) throws RuntimeException {
        // Set up an error callback. The default implementation will print the error message in System.err.
        GLFW.glfwSetErrorCallback(((error, description) -> {
            logger.error(error + ": " + description);
        }));

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        // Get the resolution of the primary monitor
        GLFWVidMode vidmode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
        assert vidmode != null;

        // Set window hints before window creation
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_POSITION_X, (vidmode.width() - width) / 2);
        GLFW.glfwWindowHint(GLFW.GLFW_POSITION_Y, (vidmode.height() - height) / 2);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_DEBUG_CONTEXT, GLFW.GLFW_TRUE);

        // Create the window
        long window = GLFW.glfwCreateWindow(width, height, title, MemoryUtil.NULL, MemoryUtil.NULL);
        if (window == MemoryUtil.NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        // Make the OpenGL context current
        GLFW.glfwMakeContextCurrent(window);

        // Enable v-sync
        GLFW.glfwSwapInterval(1);

        /*
         This line is critical for LWJGL's interoperation with GLFW's OpenGL context,
         or any context that is managed externally.
         LWJGL detects the context that is current in the current thread,
         creates the GLCapabilities instance and makes the OpenGL bindings available for use.
        */
        GL.createCapabilities();

        GLUtil.setupDebugMessageCallback();

        // Mouse motion
        if (GLFW.glfwRawMouseMotionSupported()) {
            GLFW.glfwSetInputMode(window, GLFW.GLFW_RAW_MOUSE_MOTION, GLFW.GLFW_TRUE);
        }

        if (logger != null) {
            logger.info("Window created successfully!");
        }

        return window;
    }
}