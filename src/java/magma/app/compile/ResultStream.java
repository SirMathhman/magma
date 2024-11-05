package magma.app.compile;

import magma.api.result.Ok;
import magma.api.result.Result;
import magma.api.stream.Stream;

import java.util.function.BiFunction;
import java.util.function.Function;

public record ResultStream<T, E>(Stream<Result<T, E>> stream) implements Stream<Result<T, E>> {
    public <R> Result<R, E> foldResultsLeft(R initial, BiFunction<R, T, R> folder) {
        return stream.<Result<R, E>>foldLeft(new Ok<>(initial), (reResult, teResult) -> reResult.and(() -> teResult).mapValue(tuple -> folder.apply(tuple.left(), tuple.right())));
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
}
