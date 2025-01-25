package magma.api.stream;

import magma.api.option.Option;
import magma.api.result.Ok;
import magma.api.result.Result;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public record HeadedStream<T>(Head<T> head) implements Stream<T> {

    @Override
    public Option<T> foldLeft(BiFunction<T, T, T> folder) {
        return this.head.next().map(initial -> foldLeft(initial, folder));
    }

    @Override
    public <R> R foldLeft(R initial, BiFunction<R, T, R> folder) {
        var current = initial;
        while (true) {
            R finalCurrent = current;
            final var maybeNext = this.head.next()
                    .map(next -> folder.apply(finalCurrent, next))
                    .toTuple(current);

            if (maybeNext.left()) {
                current = maybeNext.right();
            } else {
                return current;
            }
        }
    }

    @Override
    public <R> Stream<R> map(Function<T, R> mapper) {
        return new HeadedStream<>(() -> this.head.next().map(mapper));
    }

    @Override
    public <R, X> Result<R, X> foldLeftToResult(R initial, BiFunction<R, T, Result<R, X>> folder) {
        return this.<Result<R, X>>foldLeft(new Ok<>(initial), (rxResult, t) -> rxResult.flatMapValue(inner -> folder.apply(inner, t)));
    }

    @Override
    public <R> Stream<R> flatMap(Function<T, Stream<R>> mapper) {
        return new HeadedStream<>(this.head.next()
                .map(mapper)
                .<Head<R>>map(inner -> new FlatMapHead<>(this.head, inner, mapper))
                .orElse(new EmptyHead<>()));
    }

    @Override
    public <C> C collect(Collector<T, C> collector) {
        return foldLeft(collector.createInitial(), collector::fold);
    }

    @Override
    public Stream<T> filter(Predicate<T> predicate) {
        return flatMap(value -> new HeadedStream<>(predicate.test(value)
                ? new SingleHead<>(value)
                : new EmptyHead<>()));
    }

    @Override
    public Stream<T> concat(Stream<T> other) {
        return new HeadedStream<>(() -> this.head.next().or(other::next));
    }

    @Override
    public Option<T> next() {
        return this.head.next();
    }
}