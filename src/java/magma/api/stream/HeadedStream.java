package magma.api.stream;

import java.util.function.BiFunction;
import java.util.function.Function;

public record HeadedStream<T>(Head<T> head) implements Stream<T> {
    @Override
    public <R> R foldLeft(R initial, BiFunction<R, T, R> folder) {
        var current = initial;
        while (true) {
            R finalCurrent = current;
            final var tuple = head.next()
                    .map(value -> folder.apply(finalCurrent, value))
                    .toTuple(current);

            if (tuple.left()) {
                current = tuple.right();
            } else {
                return current;
            }
        }
    }

    @Override
    public <R> R into(Function<Stream<T>, R> mapper) {
        return mapper.apply(this);
    }

    @Override
    public <R> Stream<R> map(Function<T, R> mapper) {
        return new HeadedStream<>(() -> head.next().map(mapper));
    }
}
