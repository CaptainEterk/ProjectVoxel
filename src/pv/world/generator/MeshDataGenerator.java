package pv.world.generator;

import org.lwjgl.BufferUtils;
import pv.settings.ConstantGameSettings;
import pv.util.logging.PVLogger;
import pv.util.position.ChunkPosition;
import pv.util.position.WorldPosition;
import pv.world.generator.worldDataService.WorldDataService;
import pv.world.structure.block.Block;
import pv.world.structure.block.BlockFace;
import pv.world.structure.meshData.GeneralMeshData;
import pv.world.structure.meshData.MeshData;
import pv.world.structure.shape.Shape;
import pv.world.structure.texture.TexturePosition;
import pv.world.structure.vertex.UniqueVertex;
import pv.world.structure.vertex.Vertex;
import pv.world.util.ShapeHelper;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MeshDataGenerator {
    private final PVLogger logger;
    private final WorldDataService worldDataService;

    public MeshDataGenerator(PVLogger logger, WorldDataService worldDataService) {
        this.logger = logger;
        this.worldDataService = worldDataService;
    }

    public MeshData generateMeshData(ChunkPosition chunkPosition) {
        // Create buffers for vertices and indices
        List<Integer> vertices = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        List<Integer> transparentVertices = new ArrayList<>();
        List<Integer> transparentIndices = new ArrayList<>();
        Map<UniqueVertex, Integer> vertexIndexMap = new HashMap<>();
        Map<UniqueVertex, Integer> transparentVertexIndexMap = new HashMap<>();

        // Iterate through all the blocks in the chunk
        for (int x = 0; x < ConstantGameSettings.CHUNK_WIDTH; x++) {
            for (int y = 0; y < ConstantGameSettings.CHUNK_HEIGHT; y++) {
                for (int z = 0; z < ConstantGameSettings.CHUNK_LENGTH; z++) {
                    WorldPosition worldPosition = chunkPosition.toWorldPosition(x, y, z);
                    Block block = worldDataService.getBlockAt(worldPosition);
                    if (block != null) {
                        if (block.isTransparent()) {
                            generateBlockMeshData(x, y, z, worldPosition, block,
                                    transparentVertices, transparentIndices, transparentVertexIndexMap);
                        } else {
                            generateBlockMeshData(x, y, z, worldPosition, block,
                                    vertices, indices, vertexIndexMap);
                        }
                    }
                }
            }
        }

        logger.info("Chunk (" + chunkPosition.x() + ", " + chunkPosition.y() + ", " + chunkPosition.z() + ") generated with " + vertices.size() / 2 + " vertices!");

        // Convert vertex int array to ByteBuffer
        ByteBuffer vertexBuffer = BufferUtils.createByteBuffer(vertices.size() * Integer.BYTES);
        for (int vertex : vertices) {
            vertexBuffer.putInt(vertex);
        }
        vertexBuffer.flip(); // Prepare buffer for reading

        // Convert index int array to ByteBuffer
        ByteBuffer indexBuffer = BufferUtils.createByteBuffer(indices.size() * Integer.BYTES);
        for (int index : indices) {
            indexBuffer.putInt(index);
        }
        indexBuffer.flip(); // Prepare buffer for reading

        // Convert vertex int array to ByteBuffer
        ByteBuffer transparentVertexBuffer =
                BufferUtils.createByteBuffer(transparentVertices.size() * Integer.BYTES);
        for (int vertex : transparentVertices) {
            transparentVertexBuffer.putInt(vertex);
        }
        transparentVertexBuffer.flip(); // Prepare buffer for reading

        // Convert index int array to ByteBuffer
        ByteBuffer transparentIndexBuffer =
                BufferUtils.createByteBuffer(transparentIndices.size() * Integer.BYTES);
        for (int index : transparentIndices) {
            transparentIndexBuffer.putInt(index);
        }
        transparentIndexBuffer.flip(); // Prepare buffer for reading

        return new GeneralMeshData(vertexBuffer, indexBuffer, transparentVertexBuffer, transparentIndexBuffer);
    }

    private void generateBlockMeshData(int x, int y, int z, WorldPosition worldPosition, Block block, List<Integer> vertices, List<Integer> indices, Map<UniqueVertex, Integer> vertexIndexMap) {
        if (shouldRenderFace(block, worldDataService.getBlockAt(worldPosition.add(0, 1, 0)), BlockFace.TOP)) {
            generateFace(x, y, z, block, BlockFace.TOP, vertices, indices, vertexIndexMap);
        }
        if (shouldRenderFace(block, worldDataService.getBlockAt(worldPosition.add(0, -1, 0)), BlockFace.BOTTOM)) {
            generateFace(x, y, z, block, BlockFace.BOTTOM, vertices, indices, vertexIndexMap);
        }
        if (shouldRenderFace(block, worldDataService.getBlockAt(worldPosition.add(0, 0, 1)), BlockFace.NORTH)) {
            generateFace(x, y, z, block, BlockFace.NORTH, vertices, indices, vertexIndexMap);
        }
        if (shouldRenderFace(block, worldDataService.getBlockAt(worldPosition.add(0, 0, -1)), BlockFace.SOUTH)) {
            generateFace(x, y, z, block, BlockFace.SOUTH, vertices, indices, vertexIndexMap);
        }
        if (shouldRenderFace(block, worldDataService.getBlockAt(worldPosition.add(1, 0, 0)), BlockFace.EAST)) {
            generateFace(x, y, z, block, BlockFace.EAST, vertices, indices, vertexIndexMap);
        }
        if (shouldRenderFace(block, worldDataService.getBlockAt(worldPosition.add(-1, 0, 0)), BlockFace.WEST)) {
            generateFace(x, y, z, block, BlockFace.WEST, vertices, indices, vertexIndexMap);
        }
    }

    private boolean shouldRenderFace(
            Block originalBlock,
            Block adjacentBlock,
            BlockFace face
    ) {
        if (adjacentBlock == null) {
            return true;
        }
        return !(originalBlock.getShape().isFaceSolid(face) && adjacentBlock.getShape().isFaceSolid(face));
    }

    private void generateFace(
            int x,
            int y,
            int z,
            Block block,
            BlockFace blockFace,
            List<Integer> vertices,
            List<Integer> indices,
            Map<UniqueVertex, Integer> vertexIndexMap
    ) {
        Shape shape = block.getShape();

        int[] uvCoordinates = block.getUVCoordinates(blockFace);

        // Define the vertices for a single face of the cube
        Vertex[] faceVertices = shape.getVerticesOnFace(blockFace);
        int[] faceIndices = shape.getIndicesOnFace(blockFace);

        for (int index : faceIndices) {
            Vertex pointPosition = faceVertices[index];
            Vertex blockInChunkPosition = new Vertex(
                    pointPosition.px() + x,
                    pointPosition.py() + y,
                    pointPosition.pz() + z
            );
            UniqueVertex vertex = new UniqueVertex(
                    blockInChunkPosition,
                    new TexturePosition(
                            uvCoordinates[index * 2],
                            uvCoordinates[index * 2 + 1]
                    ),
                    blockFace
            );

            if (!vertexIndexMap.containsKey(vertex)) {
                int data1 = ShapeHelper.packData1(blockInChunkPosition);
                int data2 =
                        ShapeHelper.packData2(0, 0, 0, blockFace, uvCoordinates[index * 2], uvCoordinates[index * 2 + 1]);
                vertexIndexMap.put(vertex, vertices.size());

                vertices.add(data1);
                vertices.add(data2);
            }
            indices.add(vertexIndexMap.get(vertex) / 2);
        }
    }
}