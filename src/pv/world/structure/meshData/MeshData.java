package pv.world.structure.meshData;

import java.nio.ByteBuffer;

public interface MeshData {
    ByteBuffer solidVertices();

    ByteBuffer solidIndices();

    ByteBuffer transparentVertices();

    ByteBuffer transparentIndices();
}