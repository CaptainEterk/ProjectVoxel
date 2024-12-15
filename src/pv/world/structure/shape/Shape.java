package pv.world.structure.shape;

import pv.world.structure.block.BlockFace;
import pv.world.structure.vertex.Vertex;

public interface Shape {
    Vertex[] getVerticesOnFace(BlockFace blockFace);
    int[] getIndicesOnFace(BlockFace blockFace);
    boolean isFaceSolid(BlockFace blockFace);
}