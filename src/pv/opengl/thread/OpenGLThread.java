package pv.opengl.thread;

import pv.camera.Camera;
import pv.opengl.generator.MeshGenerator;
import pv.opengl.renderer.Renderer;
import pv.opengl.renderer.WorldRenderer;
import pv.opengl.structure.world.World;
import pv.opengl.window.Window;
import pv.opengl.window.WindowFactory;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFWErrorCallback;
import pv.settings.ConstantGameSettings;
import pv.settings.Settings;
import pv.util.logging.PVLogger;
import pv.util.position.Position;
import pv.util.position.PriorityPosition;
import pv.util.shader.ShaderProgramHandler;
import pv.util.texture.TextureLoader;
import pv.world.structure.meshData.PriorityMeshData;

import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL15C.GL_TEXTURE0;
import static org.lwjgl.opengl.GL15C.glActiveTexture;
import static org.lwjgl.opengl.GL30C.*;

public class OpenGLThread implements Runnable {
    private final World world;
    private final BlockingQueue<Consumer<Long>> contextTasks;
    private final MeshGenerator meshGenerator;
    private final Function<Position, Double> priorityFunction;
    private final Function<Position, Integer> renderingFunction;
    private final HashSet<Position> queuedChunks;
    private final Camera camera;
    private final PVLogger logger;
    private final Settings settings;
    private final AtomicBoolean gameRunning;
    private final Consumer<Long> initialized;
    private final PriorityBlockingQueue<PriorityPosition> nonGeneratedQueue;
    private final AtomicBoolean shouldUpdateView;
    private final AtomicBoolean shouldUpdateMeshes;
    private Renderer renderer;
    private Window window;
    private ShaderProgramHandler shaderProgramHandler;
    private int texture;

    public OpenGLThread(
            World world,
            PriorityBlockingQueue<PriorityPosition> nonGeneratedQueue,
            PriorityBlockingQueue<PriorityMeshData> nonBufferizedQueue,
            BlockingQueue<Consumer<Long>> contextTasks,
            Camera camera,
            Settings settings,
            AtomicBoolean gameRunning,
            Consumer<Long> initialized,
            Function<Position, Double> priorityFunction,
            Function<Position, Integer> renderingFunction,
            AtomicBoolean shouldUpdateView,
            AtomicBoolean shouldUpdateMeshes
    ) {
        this.renderingFunction = renderingFunction;
        this.shouldUpdateView = shouldUpdateView;
        this.shouldUpdateMeshes = shouldUpdateMeshes;
        this.logger = new PVLogger("OpenGLLogger");
        this.queuedChunks = new HashSet<>();
        this.meshGenerator = new MeshGenerator(nonBufferizedQueue, logger, world, queuedChunks);
        this.world = world;
        this.contextTasks = contextTasks;
        this.camera = camera;
        this.settings = settings;
        this.gameRunning = gameRunning;
        this.initialized = initialized;
        this.nonGeneratedQueue = nonGeneratedQueue;
        this.priorityFunction = priorityFunction;
    }

    public Window createWindow(int width, int height, String title) throws RuntimeException {
        Window window = new Window(WindowFactory.createWindow(width, height, title, logger));
        window.init(width, height);
        return window;
    }

    @Override
    public void run() {
        // The window needs to be created here because in the constructor, it would generate an OpenGL context in the Launcher thread, not this thread.
        try {
            this.window = createWindow(
                    settings.getIntSetting("width", 500),
                    settings.getIntSetting("height", 500),
                    ConstantGameSettings.DEFAULT_WINDOW_TITLE
            );
            this.shaderProgramHandler = new ShaderProgramHandler();
            // TODO: Make the shaders use a property in settings
            // TODO: Make the shader strings start with the mod name (shader/pv:chunkShader)
            shaderProgramHandler.addShaderProgram("chunkShader", Map.of("assets/shaders/main_vertex_shader.vert", GL_VERTEX_SHADER, "assets/shaders/main_fragment_shader.frag", GL_FRAGMENT_SHADER));
            // TODO: Make more shaders to handle different meshes (such as particles, entities, and effects)
            this.renderer = new Renderer(new WorldRenderer(shouldUpdateMeshes, world, shaderProgramHandler.getShaderProgram("chunkShader"), null, null, null, nonGeneratedQueue, queuedChunks, priorityFunction, renderingFunction));
        } catch (Exception e) {
            gameRunning.set(false);
            throw new RuntimeException(e);
        }
        initialized.accept(window.window());

        glClearColor(0.0f, 0.61568627451f, 1.0f, 1.0f);
//        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);

        shaderProgramHandler.getShaderProgram("chunkShader").addUniformLocation("view");
        shaderProgramHandler.getShaderProgram("chunkShader").addUniformLocation("projection");
        shaderProgramHandler.getShaderProgram("chunkShader").addUniformLocation("position");

        window.show();

        // TODO: Make this stitch textures together and save texture coordinates in a string->(x, y) map.
        texture = TextureLoader.loadTexture("texture_atlas.png");

        // Enable depth testing for solid chunks
        glEnable(GL_DEPTH_TEST);

        double startFrameTime = glfwGetTime();

        while (!window.shouldClose()) {
            double endFrameTime = startFrameTime + ConstantGameSettings.FRAME_TIME;

            world.nextFrame();
            shaderProgramHandler.getShaderProgram("chunkShader").bind();

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, texture);

            // Render the world here
            renderer.render();

            // Unbind the VAO and texture
            glBindVertexArray(0);
            glBindTexture(GL_TEXTURE_2D, 0);

            glfwSwapBuffers(window.window()); // swap the color buffers

            if (shouldUpdateView.get()) {
                // Update matrices if need be
                Matrix4f projectionMatrix = getProjectionMatrix(window);
                Matrix4f viewMatrix = getViewMatrix();

                camera.getFrustum().updateFrustum(projectionMatrix, viewMatrix);
                shaderProgramHandler.getShaderProgram("chunkShader").setUniform("projection", projectionMatrix);
                shaderProgramHandler.getShaderProgram("chunkShader").setUniform("view", viewMatrix);

                shouldUpdateView.set(false);
            }

            shaderProgramHandler.getShaderProgram("chunkShader").unbind();
            world.freeStaleMeshes();

            if (!contextTasks.isEmpty()) {
                contextTasks.forEach(task -> task.accept(window.window()));
                contextTasks.clear();
            }

            // Here is where chunk bufferization happens.
            meshGenerator.bufferizeMesh(endFrameTime);

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();

            int error = glGetError();
            if (error != 0) {
                logger.error(error + "");
            }
            double fps = Math.floor(1 / (glfwGetTime() - startFrameTime));

            // Display as an integer, not a floating-point number
            logger.info(String.format("FPS: %.0f", fps));
            startFrameTime = glfwGetTime();
        }

        gameRunning.set(false);

        world.freeAll();

        GLFWErrorCallback errorCallback = glfwSetErrorCallback(null);
        if (errorCallback != null) errorCallback.free();

        // Terminate GLFW and free the error callback
        glfwTerminate();

        logger.info("Stopped");
        // Close the logger
        logger.close();
    }

    private Matrix4f getViewMatrix() {
        Matrix4f viewMatrix = new Matrix4f();
        viewMatrix.identity();

        // Apply rotation in Yaw-Pitch-Roll order
        viewMatrix.rotate(camera.getPitch(), 1, 0, 0)
                .rotate(camera.getYaw(), 0, 1, 0);

        // Translate the camera to its position
        viewMatrix.translate(camera.getX(), camera.getY(), camera.getZ());
        return viewMatrix;
    }

    public Matrix4f getProjectionMatrix(Window window) {
        return new Matrix4f().setPerspective(
                (float) Math.toRadians(camera.getFOV()),
                window.aspectRatio(),
                camera.getNear(),
                camera.getFar()
        );
    }
}