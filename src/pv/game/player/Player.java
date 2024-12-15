package pv.game.player;

import pv.camera.Camera;
import org.lwjgl.glfw.GLFW;
import pv.settings.ConstantGameSettings;
import pv.settings.Settings;
import pv.util.input.PVKeyInput;
import pv.util.input.PVMouseButtonInput;
import pv.util.input.PVMouseInput;

import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;

public class Player {
    private final Camera camera;
    private final Settings settings;

    private float speed = 0.5f;

    public Player(Camera camera, Settings settings) {
        // TODO: Add settings for this in a menu
        this.camera = camera;
        this.settings = settings;
    }

    public void tick(float deltaTime, PVKeyInput keyInput, PVMouseButtonInput mouseButtonInput, PVMouseInput mouseInput, BlockingQueue<Consumer<Long>> contextTasks) {
        if (mouseButtonInput.isMouseLocked()) {
            float moveSpeed = speed * deltaTime * ConstantGameSettings.TARGET_FPS;

            float moveRelativeX = (keyInput.isKeyPressed(GLFW.GLFW_KEY_D) ? moveSpeed : 0) -
                    (keyInput.isKeyPressed(GLFW.GLFW_KEY_A) ? moveSpeed : 0);
            float moveRelativeY = (keyInput.isKeyPressed(GLFW.GLFW_KEY_SPACE) ? moveSpeed : 0) -
                    (keyInput.isKeyPressed(GLFW.GLFW_KEY_LEFT_SHIFT) ? moveSpeed : 0);
            float moveRelativeZ = (keyInput.isKeyPressed(GLFW.GLFW_KEY_W) ? moveSpeed : 0) -
                    (keyInput.isKeyPressed(GLFW.GLFW_KEY_S) ? moveSpeed : 0);
            if (moveRelativeX != 0 || moveRelativeY != 0 || moveRelativeZ != 0) {
                camera.move(moveRelativeX, moveRelativeY, moveRelativeZ);
            }

            double deltaX = mouseInput.getDeltaX();
            double deltaY = mouseInput.getDeltaY();

            camera.rotateX((float) deltaY / settings.getFloatSetting("sensitivity", 100f) * deltaTime * ConstantGameSettings.TARGET_FPS);
            camera.rotateY((float) deltaX / settings.getFloatSetting("sensitivity", 100f) * deltaTime * ConstantGameSettings.TARGET_FPS);

            // Handle cursor escaping
            if (keyInput.isKeyPressed(GLFW.GLFW_KEY_ESCAPE)) {
                contextTasks.add(mouseButtonInput::unlockMouse);
            }
        } else if (mouseButtonInput.isMouseButtonPressed(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            contextTasks.add(mouseButtonInput::lockMouse);
        }
        mouseInput.clearDelta();
    }
}