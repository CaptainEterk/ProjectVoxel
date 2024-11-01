package launcher;

import opengl.window.Window;
import opengl.window.WindowFactory;
import settings.Settings;

import java.io.IOException;

import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.opengl.GL11C.*;

public class Launcher {
    public static void main(String[] args) throws IOException {
        System.out.println("Loading settings...");
        Settings settings = new Settings();
        settings.load();
        System.out.println("Settings loaded");
        System.out.println("Creating window...");
        Window window = new Window(WindowFactory.createWindow(500, 500, "Project Voxel"));
        System.out.println("Window created");
        window.show();

        while (!window.shouldClose()) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

            glfwSwapBuffers(window.window()); // swap the color buffers

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();
        }
    }
}