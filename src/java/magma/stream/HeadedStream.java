package magma.stream;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public record HeadedStream<T>(Head<T> head) implements Stream<T> {
    @Override
    public <R> Optional<R> foldLeft(Function<T, R> initialMapper, BiFunction<R, T, R> folder) {
        return this.head.next().map(initialMapper).map(initial -> foldLeft(initial, folder));
    }

    @Override
    public <R> Stream<R> map(Function<T, R> mapper) {
        return new HeadedStream<>(() -> this.head.next().map(mapper));
    }

    private <R> R foldLeft(R initial, BiFunction<R, T, R> folder) {
        var current = initial;
        while (true) {
            R finalCurrent = current;
            final var next = this.head.next().map(inner -> folder.apply(finalCurrent, inner));
            if (next.isPresent()) {
                current = next.get();
            } else {
                return current;
            }
        }
    }
}
