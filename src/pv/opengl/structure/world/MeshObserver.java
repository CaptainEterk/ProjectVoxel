package pv.opengl.structure.world;

import pv.opengl.structure.mesh.Mesh;
import pv.util.position.Position;

public interface MeshObserver {
    void addMesh(Position position, Mesh mesh);
    void removeMesh(Position position, Mesh mesh);
}