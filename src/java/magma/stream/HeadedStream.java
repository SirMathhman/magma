package magma.stream;

import magma.Tuple;
import magma.option.Option;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public record HeadedStream<T>(Head<T> head) implements Stream<T> {
    @Override
    public <R> Stream<R> map(Function<T, R> mapper) {
        return new HeadedStream<>(() -> this.head.next().map(mapper));
    }

    @Override
    public <R> Stream<R> flatMap(Function<T, Stream<R>> mapper) {
        return this.<Stream<R>>foldLeft(new HeadedStream<>(new EmptyHead<>()), (rHeadedStream, other) -> rHeadedStream.concat((mapper.apply(other))));
    }

    @Override
    public <R> R foldLeft(R initial, BiFunction<R, T, R> folder) {
        var current = initial;
        while (true) {
            var finalCurrent = current;
            final var next = this.head.next()
                    .map(inner -> folder.apply(finalCurrent, inner))
                    .toTuple(current);

            if (next.left()) {
                current = next.right();
            } else {
                return current;
            }
        }
    }

    @Override
    public <R> Option<R> foldLeftWithInit(Function<T, R> initial, BiFunction<R, T, R> folder) {
        return this.head.next().map(initial).map(next -> foldLeft(next, folder));
    }

    @Override
    public Option<T> next() {
        return this.head.next();
    }

    @Override
    public Stream<T> concat(Stream<T> other) {
        return new HeadedStream<>(() -> this.head.next().or(other::next));
    }

    @Override
    public Stream<T> filter(Predicate<T> predicate) {
        return flatMap(value -> new HeadedStream<>(predicate.test(value)
                ? new SingleHead<>(value)
                : new EmptyHead<>()));
    }

    @Override
    public <C> C collect(Collector<T, C> collector) {
        return foldLeft(collector.createInitial(), collector::fold);
    }

    @Override
    public <R> Stream<Tuple<T, R>> extendBy(Function<T, R> mapper) {
        return map(inner -> new Tuple<>(inner, mapper.apply(inner)));
    }
}
