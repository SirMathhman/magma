package magma.stream;

import magma.result.Ok;
import magma.result.Result;

import java.util.function.BiFunction;
import java.util.function.Function;

public record ResultStream<T, E>(Stream<Result<T, E>> parent) implements Stream<Result<T, E>> {
    @Override
    public <R> Stream<R> map(Function<Result<T, E>, R> mapper) {
        return parent.map(mapper);
    }

    @Override
    public <R> R into(Function<Stream<Result<T, E>>, R> mapper) {
        return parent.into(mapper);
    }

    @Override
    public <R> R foldLeft(R initial, BiFunction<R, Result<T, E>, R> folder) {
        return parent.foldLeft(initial, folder);
    }

    public <R> Result<R, E> foldResultsLeft(R initial, BiFunction<R, T, R> folder) {
        return parent.<Result<R, E>>foldLeft(new Ok<>(initial), (reResult, teResult) -> reResult.and(() -> teResult).mapValue(tuple -> folder.apply(tuple.left(), tuple.right())));
    }
}
