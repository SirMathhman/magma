package magma.api.stream;

import magma.api.option.Option;
import magma.api.result.Ok;
import magma.api.result.Result;

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

    @Override
    public <R, E> Result<R, E> foldLeftToResult(R initial, BiFunction<R, T, Result<R, E>> folder) {
        return this.<Result<R, E>>foldLeft(new Ok<>(initial), (reResult, t) -> reResult.flatMapValue(current -> folder.apply(current, t)));
    }

    @Override
    public <R> Stream<R> flatMap(Function<T, Stream<R>> mapper) {
        return map(mapper).<Stream<R>>foldLeft(new HeadedStream<>(new EmptyHead<>()), Stream::concat);
    }

    @Override
    public Option<T> next() {
        return head.next();
    }

    @Override
    public Stream<T> concat(Stream<T> other) {
        return new HeadedStream<>(() -> head.next().or(other::next));
    }
}
