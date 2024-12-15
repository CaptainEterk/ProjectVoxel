package pv.launcher;

import pv.camera.Camera;
import pv.camera.Frustum;
import pv.game.thread.GameLoop;
import pv.opengl.structure.world.World;
import pv.opengl.thread.OpenGLThread;
import pv.settings.Settings;
import pv.tick.thread.TickLoop;
import pv.util.input.PVKeyInput;
import pv.util.input.PVMouseButtonInput;
import pv.util.input.PVMouseInput;
import pv.util.logging.PVLogger;
import pv.util.noise.FractionalBrownianNoise;
import pv.util.noise.PerlinNoise;
import pv.util.position.ChunkPosition;
import pv.util.position.Position;
import pv.util.position.PriorityPosition;
import pv.util.position.WorldPosition;
import pv.world.generator.worldDataService.BasicWorldDataService;
import pv.world.generator.worldDataService.CachedWorldDataService;
import pv.world.structure.block.StoneBlock;
import pv.world.structure.meshData.PriorityMeshData;
import pv.world.structure.shape.BlockShape;
import pv.world.thread.MeshDataGeneratorThread;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;

public final class Launcher {
    // State
    private final AtomicBoolean gameRunning = new AtomicBoolean(true);
    private final AtomicBoolean shouldUpdateView = new AtomicBoolean(true);
    private final AtomicBoolean shouldUpdateMeshes = new AtomicBoolean(true);
    // Thread
    private Thread openGLThread;
    private Thread meshDataGeneratorThread;
    private Thread gameLoopThread;
    private Thread tickLoopThread;
    private PVLogger logger;
    private Settings settings;
    private Camera camera;
    private Function<Position, Double> priorityFunction;
    // Queues
    private PriorityBlockingQueue<PriorityPosition> nonGeneratedQueue;
    private PriorityBlockingQueue<PriorityMeshData> nonBufferizedQueue;
    private BlockingQueue<Consumer<Long>> contextTasks;

    public static void main(String[] args) throws IOException {
        PrintStream err = System.err;
        System.setErr(new PrintStream(err) {

        });

        new Launcher().launch();
    }

    public void launch() throws IOException {
        logger = new PVLogger("Launcher");

        World world = new World();

        this.camera = new Camera(new Frustum(), shouldUpdateView, shouldUpdateMeshes);

        nonGeneratedQueue = new PriorityBlockingQueue<>();
        nonBufferizedQueue = new PriorityBlockingQueue<>();
        contextTasks = new LinkedBlockingQueue<>();

        Files.createDirectories(Path.of("/run"));

        logger.info("Loading settings...");
        this.settings = new Settings();
        settings.load();
        logger.info("Settings loaded");

        // Priority function
        this.priorityFunction = position -> {
            WorldPosition worldPosition;
            if (position instanceof ChunkPosition chunkPosition) {
                worldPosition = chunkPosition.toWorldPosition(0, 0, 0);
            } else {
                worldPosition = (WorldPosition) position;
            }
            // The closer a chunk is the higher the priority
            long distanceX = worldPosition.x() - Math.round(camera.getX());
            long distanceY = worldPosition.y() - Math.round(camera.getY());
            long distanceZ = worldPosition.z() - Math.round(camera.getZ());
            return Math.sqrt(
                    distanceX * distanceX +
                            distanceY * distanceY +
                            distanceZ * distanceZ
            );
        };

        Function<Position, Integer> renderingFunction = position -> {
            int chunkRenderDistance = settings.getIntSetting("render_distance", 2);
            double distance = priorityFunction.apply(position);
            if (distance < chunkRenderDistance) {
                if (camera.getFrustum().isMeshInFrustum(position)) {
                    return 2;
                }
                return 1;
            }
            return 0;
        };

        OpenGLThread openGL = new OpenGLThread(world, nonGeneratedQueue, nonBufferizedQueue, contextTasks, camera, settings, gameRunning, this::startOtherThreads, priorityFunction, renderingFunction, shouldUpdateView, shouldUpdateMeshes);
        this.openGLThread = new Thread(openGL, "OpenGL Thread");
        logger.info("OpenGL Thread created");

        logger.info("Starting...");
        openGLThread.start();
        logger.info("OpenGL Thread started");
    }

    // OpenGL needs to be initialized before anything else, so after it is, this method will be called
    public void startOtherThreads(long window) {
        // Input
        PVKeyInput keyInput = new PVKeyInput();
        keyInput.init(window);

        PVMouseButtonInput mouseButtonInput = new PVMouseButtonInput();
        mouseButtonInput.init(window);

        PVMouseInput mouseInput = new PVMouseInput();
        mouseInput.init(window);

        // Random
        // TODO: Make the seed a setting
        Random random = new Random(0);

        BasicWorldDataService basicWorldDataService = new BasicWorldDataService(new FractionalBrownianNoise(new PerlinNoise(random.nextLong()), 2, 0.5, 2.0, 0.02));
        // TODO: Change this to a mod loader
        basicWorldDataService.addBlock("core", new StoneBlock(new BlockShape()));

        MeshDataGeneratorThread meshDataGenerator = new MeshDataGeneratorThread(nonGeneratedQueue, nonBufferizedQueue, new CachedWorldDataService(basicWorldDataService), priorityFunction, gameRunning, new PVLogger("MeshDataGenerator"));
        this.meshDataGeneratorThread = new Thread(meshDataGenerator, "MeshDataGenerator Thread");
        logger.info("MeshDataGenerator Thread created");

        GameLoop gameLoop = new GameLoop(settings, gameRunning, contextTasks, camera, keyInput, mouseButtonInput, mouseInput);
        this.gameLoopThread = new Thread(gameLoop, "GameLoop Thread");
        logger.info("GameLoop Thread created");

        TickLoop tickLoop = new TickLoop(settings, gameRunning);
        this.tickLoopThread = new Thread(tickLoop, "TickLoop Thread");
        logger.info("TickLoop Thread created");

        meshDataGeneratorThread.start();
        logger.info("MeshDataGenerator Thread started");

        gameLoopThread.start();
        logger.info("GameLoop Thread started");

        tickLoopThread.start();
        logger.info("TickLoop Thread started");

        // Close the logger
//        logger.close();
    }
}