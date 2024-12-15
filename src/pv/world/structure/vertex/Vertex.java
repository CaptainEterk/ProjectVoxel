package pv.world.structure.vertex;

public record Vertex(int px, int py, int pz) {
    @Override
    public String toString() {
        return "Vertex{" +
                "px=" + px +
                ", py=" + py +
                ", pz=" + pz +
                '}';
    }
}