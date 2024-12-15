package pv.world.structure.shape;

import pv.world.structure.block.BlockFace;
import pv.world.structure.vertex.Vertex;

public class BlockShape implements Shape {
    private final Vertex[] topFace;
    private final Vertex[] bottomFace;
    private final Vertex[] northFace;
    private final Vertex[] southFace;
    private final Vertex[] eastFace;
    private final Vertex[] westFace;
    private final int[] indices;

    public BlockShape() {
        this.topFace = new Vertex[]{
                new Vertex(0, 1, 0)
                , new Vertex(0, 1, 1)
                , new Vertex(1, 1, 1)
                , new Vertex(1, 1, 0)
        };
        this.bottomFace = new Vertex[]{
                new Vertex(0, 0, 0)
                , new Vertex(1, 0, 0)
                , new Vertex(1, 0, 1)
                , new Vertex(0, 0, 1)
        };
        this.northFace = new Vertex[]{
                new Vertex(0, 0, 1)
                , new Vertex(1, 0, 1)
                , new Vertex(1, 1, 1)
                , new Vertex(0, 1, 1)
        };
        this.southFace = new Vertex[]{
                new Vertex(0, 1, 0)
                , new Vertex(1, 1, 0)
                , new Vertex(1, 0, 0)
                , new Vertex(0, 0, 0)
        };
        this.eastFace = new Vertex[]{
                new Vertex(1, 1, 0)
                , new Vertex(1, 1, 1)
                , new Vertex(1, 0, 1)
                , new Vertex(1, 0, 0)
        };
        this.westFace = new Vertex[]{
                new Vertex(0, 0, 0)
                , new Vertex(0, 0, 1)
                , new Vertex(0, 1, 1)
                , new Vertex(0, 1, 0)
        };
        this.indices = new int[]{
                0, 1, 2,
                0, 2, 3
        };
    }

    @Override
    public Vertex[] getVerticesOnFace(BlockFace blockFace) {
        return switch (blockFace) {
            case TOP -> topFace;
            case BOTTOM -> bottomFace;
            case NORTH -> northFace;
            case SOUTH -> southFace;
            case EAST -> eastFace;
            case WEST -> westFace;
        };
    }

    @Override
    public int[] getIndicesOnFace(BlockFace blockFace) {
        return indices;
    }

    @Override
    public boolean isFaceSolid(BlockFace blockFace) {
        return true;
    }
}