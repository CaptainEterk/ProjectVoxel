package pv.util.position;

import pv.settings.ConstantGameSettings;

public record ChunkPosition(int x, int y, int z) implements Position {
    public WorldPosition toWorldPosition(int x, int y, int z) {
        return new WorldPosition(
                (long) this.x * ConstantGameSettings.CHUNK_WIDTH + x,
                (long) this.y * ConstantGameSettings.CHUNK_HEIGHT + y,
                (long) this.z * ConstantGameSettings.CHUNK_LENGTH + z
        );
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ChunkPosition that = (ChunkPosition) o;
        return x == that.x && y == that.y && z == that.z;
    }
}