package magma.api.stream;

import magma.api.option.Option;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

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

    @Override
    public Option<T> next() {
        return head.next();
    }

    public <R> Stream<R> flatMap(Function<T, Head<R>> mapper) {
        return new HeadedStream<>(this.foldLeft(new EmptyHead<>(), (BiFunction<Head<R>, T, Head<R>>) (rHead, t) -> new ConcatHead<>(rHead, mapper.apply(t))));
    }

    @Override
    public Stream<T> filter(Predicate<T> predicate) {
        return flatMap(value -> predicate.test(value) ? new SingleHead<>(value) : new EmptyHead<>());
    }
}
