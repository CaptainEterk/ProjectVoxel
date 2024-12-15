package pv.util.position;

import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public record PriorityPosition(
        ChunkPosition chunkPosition,
        Function<Position, Double> priorityFunction
) implements Comparable<PriorityPosition> {
    public Double getPriority() {
        return priorityFunction.apply(chunkPosition);
    }

    @Override
    public int compareTo(@NotNull PriorityPosition priorityPosition) {
        return Double.compare(getPriority(), priorityPosition.getPriority());
    }
}