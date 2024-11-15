package magma.api.stream;

import magma.api.option.Option;
import magma.api.result.Ok;
import magma.api.result.Result;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public record ResultStream<T, E>(Stream<Result<T, E>> stream) implements Stream<Result<T, E>> {
    public static <T> Stream<T> fromOption(Option<T> option) {
        return Streams.fromOption(option);
    }

    @Override
    public <C> C collect(Collector<Result<T, E>, C> collector) {
        return stream.collect(collector);
    }

    @Override
    public Stream<Result<T, E>> filter(Predicate<Result<T, E>> filter) {
        return stream.filter(filter);
    }

    @Override
    public <R, E0> Result<R, E0> foldLeftToResult(R initial, BiFunction<R, Result<T, E>, Result<R, E0>> folder) {
        return stream.foldLeftToResult(initial, folder);
    }

    public <R> Result<R, E> foldResultsLeft(R initial, BiFunction<R, T, R> folder) {
        return stream.<Result<R, E>>foldLeft(new Ok<>(initial), (reResult, teResult) -> reResult.and(() -> teResult).mapValue(tuple -> folder.apply(tuple.left(), tuple.right())));
    }

    @Override
    public <R> Stream<R> flatMap(Function<Result<T, E>, Stream<R>> mapper) {
        return stream.flatMap(mapper);
    }

    @Override
    public Option<Result<T, E>> next() {
        return stream.next();
    }

    @Override
    public Stream<Result<T, E>> concat(Stream<Result<T, E>> other) {
        return stream.concat(other);
    }

    @Override
    public <R> R foldLeft(R initial, BiFunction<R, Result<T, E>, R> folder) {
        return stream.foldLeft(initial, folder);
    }

    @Override
    public <R> Stream<R> map(Function<Result<T, E>, R> mapper) {
        return stream.map(mapper);
    }

    @Override
    public <R> R into(Function<Stream<Result<T, E>>, R> mapper) {
        return stream.into(mapper);
    }

    @Override
    public boolean allMatch(Predicate<Result<T, E>> predicate) {
        return foldLeft(true, (aBoolean, t) -> aBoolean && predicate.test(t));
    }

    public <R> ResultStream<R, E> flatMapResult(Function<T, Result<R, E>> mapper) {
        return new ResultStream<>(map(inner -> inner.flatMapValue(mapper)));
    }
}
