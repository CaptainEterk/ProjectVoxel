package pv.world.structure.block;

import pv.world.structure.shape.BlockShape;
import pv.world.structure.shape.Shape;

// TODO: Make this a mod
public class StoneBlock implements Block {
    private final Shape shape;
    private final int[] uvCoords;

    public StoneBlock(BlockShape shape) {
        this.shape = shape;
        this.uvCoords = new int[]{
                2, 0,
                3, 0,
                3, 1,
                2, 1
        };
    }

    @Override
    public String getID() {
        return "stone_block";
    }

    @Override
    public Shape getShape() {
        return shape;
    }

    @Override
    public int[] getUVCoordinates(BlockFace blockFace) {
        return uvCoords;
    }
}