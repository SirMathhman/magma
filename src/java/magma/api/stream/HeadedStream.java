package magma.api.stream;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public record HeadedStream<T>(Head<T> head) implements Stream<T> {
    @Override
    public <R> Stream<R> map(Function<T, R> mapper) {
        return new HeadedStream<>(() -> head.next().map(mapper));
    }

    @Override
    public <R> R into(Function<Stream<T>, R> mapper) {
        return mapper.apply(this);
    }

    @Override
    public <R> R foldLeft(R initial, BiFunction<R, T, R> folder) {
        var current = initial;
        var counter = 0;
        while (counter < Integer.MAX_VALUE) {
            var finalCurrent = current;
            final var tuple = head.next()
                    .map(value -> folder.apply(finalCurrent, value))
                    .toTuple(current);

            if (tuple.left()) {
                current = tuple.right();
                counter++;
            } else {
                break;
            }
        }

        return current;
    }

    @Override
    public boolean allMatch(Predicate<T> predicate) {
        return foldLeft(true, (aBoolean, t) -> aBoolean && predicate.test(t));
    }
}
