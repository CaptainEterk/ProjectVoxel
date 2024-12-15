package pv.util.position;

public record WorldPosition(long x, long y, long z) implements Position {
    public WorldPosition add(int x, int y, int z) {
        return new WorldPosition(this.x + x, this.y + y, this.z + z);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorldPosition that = (WorldPosition) o;
        return x == that.x && y == that.y && z == that.z;
    }
}