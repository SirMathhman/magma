package magma.api.stream;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public record HeadedStream<T>(Head<T> head) implements Stream<T> {
    @Override
    public <R> Optional<R> foldLeft(Function<T, R> mapper, BiFunction<R, T, R> folder) {
        return this.head.next().map(mapper).map(initial -> foldLeft(initial, folder));
    }

    private <R> R foldLeft(R initial, BiFunction<R, T, R> folder) {
        var current = initial;
        while (true) {
            R finalCurrent = current;
            final var maybeNext = this.head.next().map(next -> folder.apply(finalCurrent, next));
            if (maybeNext.isPresent()) {
                current = maybeNext.get();
            } else {
                return current;
            }
        }
    }

    @Override
    public <R> Stream<R> map(Function<T, R> mapper) {
        return new HeadedStream<>(() -> this.head.next().map(mapper));
    }
}