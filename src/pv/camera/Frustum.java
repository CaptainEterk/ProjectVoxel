package pv.camera;

import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import pv.settings.ConstantGameSettings;
import pv.util.position.ChunkPosition;
import pv.util.position.Position;
import pv.util.position.WorldPosition;

public class Frustum {
    private final FrustumIntersection frustumIntersection = new FrustumIntersection();

    public void updateFrustum(Matrix4f projectionMatrix, Matrix4f viewMatrix) {
        // Create the combined view-projection matrix and pass it to the frustum
        Matrix4f viewProjMatrix = new Matrix4f(projectionMatrix).mul(viewMatrix);
        frustumIntersection.set(viewProjMatrix);
    }

    private boolean isChunkInFrustum(ChunkPosition chunkPosition) {
        // Convert chunk coordinates to world space by multiplying by chunkSize (32 in this case)
        WorldPosition worldChunkPosition = chunkPosition.toWorldPosition(0, 0, 0);

        // Test the AABB of the chunk in world space against the frustum
        return frustumIntersection.testAab(
                worldChunkPosition.x(),
                worldChunkPosition.y(),
                worldChunkPosition.z(),
                worldChunkPosition.x() + ConstantGameSettings.CHUNK_WIDTH,
                worldChunkPosition.y() + ConstantGameSettings.CHUNK_HEIGHT,
                worldChunkPosition.z() + ConstantGameSettings.CHUNK_LENGTH
        );
    }

    public boolean isMeshInFrustum(Position position) {
        if (position instanceof ChunkPosition chunkPosition) {
            return isChunkInFrustum(chunkPosition);
        }
        return true;
    }
}