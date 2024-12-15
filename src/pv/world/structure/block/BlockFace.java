package pv.world.structure.block;

public enum BlockFace {
    TOP,
    BOTTOM,
    NORTH,
    SOUTH,
    EAST,
    WEST;

    public BlockFace opposite() {
        return switch (this) {
            case TOP -> BOTTOM;
            case BOTTOM -> TOP;
            case NORTH -> SOUTH;
            case SOUTH -> NORTH;
            case EAST -> WEST;
            case WEST -> EAST;
        };
    }
}