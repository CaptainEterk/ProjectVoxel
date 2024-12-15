package pv.tick.thread;

import pv.settings.ConstantGameSettings;
import pv.settings.Settings;
import pv.util.logging.PVLogger;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class TickLoop implements Runnable {
    private final PVLogger logger;
    private final Settings settings;
    private final AtomicBoolean gameRunning;

    public TickLoop(Settings settings, AtomicBoolean gameRunning) {
        this.logger = new PVLogger("TickLoop");
        this.settings = settings;
        this.gameRunning = gameRunning;
    }

    @Override
    public void run() {
        logger.info("TickLoop running!");

        double lastTick = glfwGetTime();

        while (gameRunning.get()) {
            double startTime = glfwGetTime();

            // If a game tick is necessary, run it
            if (startTime - lastTick >= ConstantGameSettings.TICK_TIME) {
                tick();
                lastTick = startTime;
            } else {
                try {
                    Thread.sleep(Math.round((ConstantGameSettings.TICK_TIME - (startTime - lastTick)) * 1000));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        logger.close();
    }

    private void tick() {
        logger.info("Tick!");
    }
}