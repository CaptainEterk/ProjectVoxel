package pv.world.thread;

import pv.settings.ConstantGameSettings;
import pv.util.logging.PVLogger;
import pv.util.position.Position;
import pv.util.position.PriorityPosition;
import pv.world.generator.MeshDataGenerator;
import pv.world.generator.worldDataService.WorldDataService;
import pv.world.structure.meshData.PriorityMeshData;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

public class MeshDataGeneratorThread implements Runnable {
    private final PriorityBlockingQueue<PriorityPosition> nonGeneratedQueue;
    private final PriorityBlockingQueue<PriorityMeshData> generatedQueue;
    private final WorldDataService worldDataService;
    private final boolean clone;
    private int generatorCount = 1;
    private final Runnable stop;
    private final AtomicBoolean gameRunning;
    private final Function<Position, Double> priorityFunction;
    private final PVLogger logger;
    private final MeshDataGenerator meshDataGenerator;

    public MeshDataGeneratorThread(
            PriorityBlockingQueue<PriorityPosition> nonGeneratedQueue,
            PriorityBlockingQueue<PriorityMeshData> generatedQueue,
            WorldDataService worldDataService,
            Function<Position, Double> priorityFunction,
            AtomicBoolean gameRunning,
            PVLogger logger
    ) {
        this(nonGeneratedQueue, generatedQueue, worldDataService, priorityFunction, gameRunning, null, logger, false, new MeshDataGenerator(logger, worldDataService));
    }

    // Clone constructor
    private MeshDataGeneratorThread(
            PriorityBlockingQueue<PriorityPosition> nonGeneratedQueue,
            PriorityBlockingQueue<PriorityMeshData> generatedQueue,
            WorldDataService worldDataService,
            Function<Position, Double> priorityFunction,
            AtomicBoolean gameRunning,
            Runnable stop,
            PVLogger logger,
            boolean clone,
            MeshDataGenerator meshDataGenerator
    ) {
        this.nonGeneratedQueue = nonGeneratedQueue;
        this.generatedQueue = generatedQueue;
        this.worldDataService = worldDataService;
        this.stop = stop;
        this.gameRunning = gameRunning;
        this.priorityFunction = priorityFunction;
        this.clone = clone;
        this.logger = logger;
        this.meshDataGenerator = meshDataGenerator;
    }

    @Override
    public void run() {
        while ((!nonGeneratedQueue.isEmpty() || !clone) && gameRunning.get()) {
            PriorityPosition position = nonGeneratedQueue.poll();
            if (position != null) {
                generatedQueue.add(
                        new PriorityMeshData(
                                position.chunkPosition(),
                                meshDataGenerator.generateMeshData(position.chunkPosition()),
                                priorityFunction
                        )
                );
                if (!clone && nonGeneratedQueue.size() > ConstantGameSettings.NON_GENERATED_QUEUE_OVERLOAD_LIMIT * generatorCount) {
                    Thread newGenerator = new Thread(new MeshDataGeneratorThread(nonGeneratedQueue, generatedQueue, worldDataService, priorityFunction, gameRunning, () -> generatorCount--, logger, true, meshDataGenerator), "MeshDataGenerator [CLONE (" + (generatorCount - 1) + ")]");
                    newGenerator.start();
                    generatorCount++;
                }
            } else if (!clone) {
                while (gameRunning.get() && nonGeneratedQueue.isEmpty()) {
                    Thread.onSpinWait();
                }
            } else {
                // If you are a clone and there are no more items in the queue, stop running the loop
                break;
            }
        }
        // If you are outside the loop and a clone, stop.
        if (clone) {
            stop.run();
        }
    }
}
