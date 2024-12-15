package pv.opengl.structure.mesh;

public interface Mesh {
    int solidVAO();
    int solidIndexCount();
    int transparentVAO();
    int transparentIndexCount();
}