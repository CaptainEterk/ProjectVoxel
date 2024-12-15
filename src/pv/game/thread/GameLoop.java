package pv.game.thread;

import pv.camera.Camera;
import pv.game.player.Player;
import pv.settings.ConstantGameSettings;
import pv.settings.Settings;
import pv.util.input.PVKeyInput;
import pv.util.input.PVMouseButtonInput;
import pv.util.input.PVMouseInput;
import pv.util.logging.PVLogger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class GameLoop implements Runnable {
    private final PVLogger logger;
    private final Settings settings;
    private final AtomicBoolean gameRunning;

    private final BlockingQueue<Consumer<Long>> contextTasks;
    private final Player player;
    private final PVKeyInput keyInput;
    private final PVMouseButtonInput mouseButtonInput;
    private final PVMouseInput mouseInput;

    public GameLoop(Settings settings, AtomicBoolean gameRunning, BlockingQueue<Consumer<Long>> contextTasks, Camera camera, PVKeyInput keyInput, PVMouseButtonInput mouseButtonInput, PVMouseInput mouseInput) {
        this.logger = new PVLogger("GameLoop");
        this.settings = settings;
        this.gameRunning = gameRunning;
        this.contextTasks = contextTasks;
        this.player = new Player(camera, settings);
        this.keyInput = keyInput;
        this.mouseButtonInput = mouseButtonInput;
        this.mouseInput = mouseInput;
    }

    @Override
    public void run() {
        logger.info("GameLoop running!");

        double oldTime = glfwGetTime();

        while (gameRunning.get()) {
            double startTime = glfwGetTime();

            // Every second ConstantGameSettings.FRAME_TIME ticks will be called
            if (startTime - oldTime >= ConstantGameSettings.FRAME_TIME) {
                tick(startTime - oldTime);
                oldTime = startTime;
            } else {
                try {
                    Thread.sleep(Math.round((ConstantGameSettings.FRAME_TIME - (startTime - oldTime)) * 1000));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        logger.close();
    }

    private void tick(double deltaTime) {
        player.tick((float) deltaTime, keyInput, mouseButtonInput, mouseInput, contextTasks);
    }
}