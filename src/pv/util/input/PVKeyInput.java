package pv.util.input;

import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

public class PVKeyInput {
    private final Map<Integer, Boolean> keys;

    public PVKeyInput() {
        keys = new HashMap<>();
    }

    public void init(long window) {
        GLFW.glfwSetKeyCallback(window, this::keyCallback);
    }

    private void keyCallback(long window, int key, int scancode, int action, int mods) {
        if (action == GLFW_PRESS) {
            keys.put(key, true);
        } else if (action == GLFW_RELEASE) {
            keys.put(key, false);
        }
    }

    public boolean isKeyPressed(int key) {
        return keys.getOrDefault(key, false);
    }
}