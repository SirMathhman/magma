package magma.api.stream.head;

import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.api.stream.Stream;
import magma.api.stream.Streams;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public record HeadedStream<T>(Head<T> head) implements Stream<T> {
    @Override
    public <R, X> Result<R, X> foldLeftIntoResult(R initial, BiFunction<R, T, Result<R, X>> folder) {
        return this.<Result<R, X>>foldLeft(new Ok<>(initial), (current, next) -> current.flatMapValue(currentValue -> folder.apply(currentValue, next)));
    }

    @Override
    public <R> Stream<R> map(Function<T, R> mapper) {
        return new HeadedStream<>(() -> head.next().map(mapper));
    }

    @Override
    public <R> Stream<R> flatMap(Function<T, Stream<R>> mapper) {
        return map(mapper).<Stream<R>>foldLeft(new HeadedStream<>(None::new), Stream::concat);
    }

    @Override
    public Stream<T> concat(Stream<T> other) {
        return new HeadedStream<>(() -> head.next().or(other::next));
    }

    @Override
    public Option<T> next() {
        return head.next();
    }

    @Override
    public <R> R foldLeft(R initial, BiFunction<R, T, R> folder) {
        var current = initial;
        while (true) {
            var finalCurrent = current;
            final var tuple = head.next()
                    .map(headValue -> folder.apply(finalCurrent, headValue))
                    .toTuple(current);

            if (tuple.left()) {
                current = tuple.right();
            } else {
                return current;
            }
        }
    }

    @Override
    public Option<T> foldLeft(BiFunction<T, T, T> folder) {
        return head.next().map(value -> this.foldLeft(value, folder));
    }

    @Override
    public Stream<T> filter(Predicate<T> predicate) {
        return this.<Option<T>>map(Some::new)
                .map(option -> option.filter(predicate))
                .flatMap(Streams::fromOption);
    }
}
