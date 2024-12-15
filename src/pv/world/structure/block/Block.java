package pv.world.structure.block;

import pv.world.structure.shape.Shape;

public interface Block {
    String getID();
    default boolean isTransparent() {
        return false;
    }
    Shape getShape();
    int[] getUVCoordinates(BlockFace blockFace);
}