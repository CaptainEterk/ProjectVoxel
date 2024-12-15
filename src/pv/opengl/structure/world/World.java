package pv.opengl.structure.world;

import pv.opengl.structure.mesh.Mesh;
import pv.settings.ConstantGameSettings;
import pv.util.position.Position;

import java.util.*;

import static org.lwjgl.opengl.GL30C.glDeleteVertexArrays;

class PositionEntry {
    private final Mesh mesh;
    private int lastAccessedFrame;

    public PositionEntry(Mesh mesh, int lastAccessedFrame) {
        this.mesh = mesh;
        this.lastAccessedFrame = lastAccessedFrame;
    }

    // Updates the last accessed frame
    public void updateLastAccess(int currentFrame) {
        this.lastAccessedFrame = currentFrame;
    }

    public Mesh getMesh() {
        return mesh;
    }

    public int getLastAccessedFrame() {
        return lastAccessedFrame;
    }
}

// TODO: This should also include chunks that you visit often, in some sort of chunk cache.
public class World {
    private final List<MeshObserver> meshObservers;
    private final Map<Position, PositionEntry> world = new HashMap<>();
    private int currentFrame = 0;

    // Constructor with maxFramesWithoutAccess
    public World() {
        meshObservers = new ArrayList<>();
    }

    // Adds a Mesh to the specified Position
    public void addMesh(Position position, Mesh mesh) {
        world.put(position, new PositionEntry(mesh, currentFrame));
        meshObservers.forEach(meshObserver -> meshObserver.addMesh(position, mesh));
    }

    // Retrieves a Mesh by Position and updates its last accessed frame
    public Mesh getMesh(Position position) {
        PositionEntry entry = world.get(position);
        if (entry == null) return null;

        // Update last accessed frame to the current frame
        entry.updateLastAccess(currentFrame);
        return entry.getMesh();
    }

    // Called at each new frame to increment the frame count
    public void nextFrame() {
        currentFrame++;
    }

    // Removes stale meshes that haven't been accessed in the last maxFramesWithoutAccess frames
    public void freeStaleMeshes() {
        Set<Position> keySet = Set.copyOf(world.keySet());
        keySet.forEach(position -> {
            if (currentFrame - world.get(position).getLastAccessedFrame() > ConstantGameSettings.MESH_REQUEST_STALE_LIMIT) {
                System.out.println("Stale mesh!");
                free(position);
            }
        });
    }

    public void free(Position position) {
        PositionEntry positionEntry = world.remove(position);
        if (positionEntry != null) {
            Mesh mesh = positionEntry.getMesh();
            glDeleteVertexArrays(mesh.solidVAO());
            glDeleteVertexArrays(mesh.transparentVAO());
            meshObservers.forEach(meshObserver -> meshObserver.removeMesh(position, mesh));
        }
    }

    public void freeAll() {
        Set<Position> keySet = Set.copyOf(world.keySet());
        keySet.forEach(this::free);
    }

    public void addMeshObserver(MeshObserver meshObserver) {
        meshObservers.add(meshObserver);
    }
}