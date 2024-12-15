package pv.opengl.structure.mesh;

public record GeneralMesh(int solidVAO, int solidIndexCount,
                          int transparentVAO, int transparentIndexCount
) implements Mesh {
}