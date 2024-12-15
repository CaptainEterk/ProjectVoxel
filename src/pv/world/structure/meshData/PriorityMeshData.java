package pv.world.structure.meshData;

import org.jetbrains.annotations.NotNull;
import pv.util.position.Position;

import java.util.function.Function;

public record PriorityMeshData(
        Position position,
        MeshData meshData,
        Function<Position, Double> priorityFunction
) implements Comparable<PriorityMeshData> {
    public Double getPriority() {
        return priorityFunction.apply(position);
    }

    @Override
    public int compareTo(@NotNull PriorityMeshData priorityMeshData) {
        return Double.compare(getPriority(), priorityMeshData.getPriority());
    }
}