package pv.opengl.renderer;

import pv.opengl.structure.mesh.Mesh;
import pv.opengl.structure.world.MeshObserver;
import pv.opengl.structure.world.World;
import pv.settings.ConstantGameSettings;
import pv.util.position.ChunkPosition;
import pv.util.position.Position;
import pv.util.position.PriorityPosition;
import pv.util.shader.ShaderProgram;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;

/**
 * This class renders chunks, particles, entities, and effects.
 * This class does not render text.
 */
public class WorldRenderer {
    private final List<MeshPosition> meshes;
    private final World world;
    private final ShaderProgram chunkShader;
    private final ShaderProgram particleShader;
    private final ShaderProgram entityShader;
    private final ShaderProgram effectShader;
    private final PriorityBlockingQueue<PriorityPosition> nonGeneratedQueue;
    private final Set<Position> queuedChunks;
    private final Function<Position, Double> priorityFunction;
    private final Function<Position, Integer> renderingFunction;
    private final AtomicBoolean shouldUpdateMeshes;
    private int currentRenderDistance = 0;

    public WorldRenderer(AtomicBoolean shouldUpdateMeshes, World world, ShaderProgram chunkShaderProgram, ShaderProgram particleShader, ShaderProgram entityShader, ShaderProgram effectShader, PriorityBlockingQueue<PriorityPosition> nonGeneratedQueue, Set<Position> queuedChunks, Function<Position, Double> priorityFunction, Function<Position, Integer> renderingFunction) {
        this.shouldUpdateMeshes = shouldUpdateMeshes;
        this.world = world;
        this.chunkShader = chunkShaderProgram;
        this.particleShader = particleShader;
        this.entityShader = entityShader;
        this.effectShader = effectShader;
        this.nonGeneratedQueue = nonGeneratedQueue;
        this.queuedChunks = queuedChunks;
        this.priorityFunction = priorityFunction;
        this.renderingFunction = renderingFunction;
        world.addMeshObserver(new MeshObserver() {
            @Override
            public void addMesh(Position position, Mesh mesh) {
//                calculateRenderedChunks(currentRenderDistance);
            }

            @Override
            public void removeMesh(Position position, Mesh mesh) {

            }
        });
        meshes = new ArrayList<>();
    }

    private void calculateRenderedChunks(int distance) {
        int distanceX = Math.round((float) distance / ConstantGameSettings.CHUNK_WIDTH);
        int distanceY = Math.round((float) distance / ConstantGameSettings.CHUNK_HEIGHT);
        int distanceZ = Math.round((float) distance / ConstantGameSettings.CHUNK_LENGTH);
        meshes.clear();
        for (int x = -distanceX; x < distanceX; x++) {
            for (int y = -distanceY; y < distanceY; y++) {
                for (int z = -distanceZ; z < distanceZ; z++) {
                    ChunkPosition chunkPosition = new ChunkPosition(x, y, z);
                    int chunkRenderingState = renderingFunction.apply(chunkPosition);
                    if (chunkRenderingState > 0) {
                        addChunk(chunkPosition, chunkRenderingState == 2);
                    }
                }
            }
        }
    }

    private void addChunk(ChunkPosition chunkPosition, boolean render) {
        Mesh mesh = world.getMesh(chunkPosition);
        if (mesh == null) {
            if (!queuedChunks.contains(chunkPosition)) {
                nonGeneratedQueue.add(new PriorityPosition(chunkPosition, priorityFunction));
                queuedChunks.add(chunkPosition);
            }
        } else if (render) {
            meshes.add(new MeshPosition(chunkPosition, mesh, priorityFunction.apply(chunkPosition)));
        }
    }

    public void render() {
        // Render chunks (solid)
        meshes.forEach(this::renderMesh);
        if (currentRenderDistance < 100) {
            currentRenderDistance++;
        } else if (currentRenderDistance > 100) {
            currentRenderDistance = 100;
        }
        calculateRenderedChunks(currentRenderDistance);
        shouldUpdateMeshes.set(false);
    }

    private void renderMesh(MeshPosition meshPosition) {
        renderMesh(meshPosition.position(), meshPosition.mesh());
    }

    private void renderMesh(Position position, Mesh mesh) {
        if (position instanceof ChunkPosition chunkPosition) {
            chunkShader.setUniform("position", chunkPosition.x(), chunkPosition.y(), chunkPosition.z());

            // Set up transformations here (if applicable) based on worldPosition
            // This is where you could apply translation, rotation, etc., using OpenGL transforms.

            // Render solid part of the mesh
            renderVAO(mesh.solidVAO(), mesh.solidIndexCount());

            // TODO: Separate solid and transparent chunks. Render all the solid chunks first. Then render the transparent ones.
            // Render transparent part of the mesh
            renderVAO(mesh.transparentVAO(), mesh.transparentIndexCount());
        }
    }

    private void renderVAO(int vao, int indexCount) {
        if (vao > 0 && indexCount > 0) {
            // Bind the VAO
            glBindVertexArray(vao);

            // Draw the elements using indices in the VAO
            glDrawElements(GL_TRIANGLES, indexCount, GL_UNSIGNED_INT, 0);
        }
    }

    private record MeshPosition(Position position, Mesh mesh, double priority) {
    }
}