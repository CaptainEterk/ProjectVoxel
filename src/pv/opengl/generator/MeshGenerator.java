package pv.opengl.generator;

import pv.opengl.structure.mesh.GeneralMesh;
import pv.opengl.structure.world.World;
import pv.util.logging.PVLogger;
import pv.util.position.Position;
import pv.world.structure.meshData.MeshData;
import pv.world.structure.meshData.PriorityMeshData;

import java.nio.ByteBuffer;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;

import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.opengl.GL11C.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL20C.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL30C.*;

public final class MeshGenerator {
    private final PriorityBlockingQueue<PriorityMeshData> nonBufferizedQueue;

    private final PVLogger logger;
    private final World world;

    private final Set<Position> queuedMeshes;

    private double estimatedBufferTime;

    public MeshGenerator(PriorityBlockingQueue<PriorityMeshData> nonBufferizedQueue, PVLogger logger, World world, Set<Position> queuedMeshes) {
        this.nonBufferizedQueue = nonBufferizedQueue;
        this.logger = logger;
        this.world = world;
        this.queuedMeshes = queuedMeshes;
    }

    public void bufferizeMesh(double endFrameTime) {
        PriorityMeshData priorityMeshData = nonBufferizedQueue.poll();
        if (priorityMeshData != null) {
            double startBufferTime = glfwGetTime();
            MeshData meshData = priorityMeshData.meshData();
            GeneralMesh generalMesh = new GeneralMesh(
                    generateVAO(meshData.solidVertices(), meshData.solidIndices()),
                    meshData.solidIndices().capacity() / 4,
                    generateVAO(meshData.transparentVertices(), meshData.transparentIndices()),
                    meshData.transparentIndices().capacity() / 4
            );
            world.addMesh(priorityMeshData.position(), generalMesh);
            queuedMeshes.remove(priorityMeshData.position());
            // There might be other chunks to be bufferized. This tries to bufferize another chunk if it won't take too long.
            double endBufferTime = glfwGetTime();
            estimatedBufferTime = endBufferTime - startBufferTime;

            logger.info("Mesh " + priorityMeshData.position() + " bufferized in " + Math.round(estimatedBufferTime * 1000) + " ms");

            if (endBufferTime + estimatedBufferTime < endFrameTime) {
                // Still time left in the frame to bufferize chunks
                bufferizeMesh(endFrameTime);
            }
        }
    }

    private int generateVAO(ByteBuffer vertexBuffer, ByteBuffer indexBuffer) {
        // Generate and bind VAO
        int vao = glGenVertexArrays();
        glBindVertexArray(vao);

        // Generate and bind VBO for vertex data
        int vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

        // Generate and bind EBO for index data
        int ebo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);

        // Specify the layout of the vertex data
        // Assuming the vertex data is interleaved with data1 (uint) and data2 (uint)

        // Data1 attribute (1 uint)
        glVertexAttribIPointer(0, 1, GL_UNSIGNED_INT, 2 * Integer.BYTES, 0);
        glEnableVertexAttribArray(0);

        // Data2 attribute (1 uint)
        glVertexAttribIPointer(1, 1, GL_UNSIGNED_INT, 2 * Integer.BYTES, Integer.BYTES);
        glEnableVertexAttribArray(1);

        // Unbind VBO and EBO
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        return vao;
    }
}