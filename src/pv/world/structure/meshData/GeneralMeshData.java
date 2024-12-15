package pv.world.structure.meshData;

import java.nio.ByteBuffer;

public record GeneralMeshData(ByteBuffer solidVertices, ByteBuffer solidIndices, ByteBuffer transparentVertices, ByteBuffer transparentIndices) implements MeshData {
}