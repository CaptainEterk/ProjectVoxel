package pv.world.structure.vertex;

import pv.world.structure.block.BlockFace;
import pv.world.structure.texture.TexturePosition;

public record UniqueVertex(Vertex vertex, TexturePosition texturePosition, BlockFace blockFace) {


    @Override
    public boolean equals(Object o) {
        if (!(o instanceof UniqueVertex other)) {
            return false;
        }
        Vertex otherVertex = other.vertex();
        // Vertex
        if (vertex.px() != otherVertex.px() ||
                vertex.py() != otherVertex.py() ||
                vertex.pz() != otherVertex.pz()
        ) {
            return false;
        }
        TexturePosition otherTexturePosition = other.texturePosition();
        // TexturePosition
        if (
                texturePosition.tx() != otherTexturePosition.tx() ||
                        texturePosition.ty() != otherTexturePosition.ty()
        ) {
            return false;
        }
        // BlockFace
        return blockFace == other.blockFace();
    }
}