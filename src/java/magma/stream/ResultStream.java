package magma.stream;

import magma.result.Ok;
import magma.result.Result;

import java.util.function.BiFunction;
import java.util.function.Function;

public record ResultStream<T, X>(Stream<Result<T, X>> parent) implements Stream<Result<T, X>> {
    @Override
    public <R> Stream<R> map(Function<Result<T, X>, R> mapper) {
        return parent.map(mapper);
    }

    @Override
    public <C> C foldLeft(C initial, BiFunction<C, Result<T, X>, C> folder) {
        return parent.foldLeft(initial, folder);
    }

    @Override
    public <R> R into(Function<Stream<Result<T, X>>, R> mapper) {
        return parent.into(mapper);
    }

    public <C> Result<C, X> foldResultsLeft(C initial, BiFunction<C, T, C> folder) {
        return parent.<Result<C, X>>foldLeft(new Ok<>(initial), (cxResult, txResult) -> cxResult.and(() -> txResult).mapValue(tuple -> folder.apply(tuple.left(), tuple.right())));
    }

    public <R> ResultStream<R, X> mapResult(Function<T, R> mapper) {
        return new ResultStream<>(parent.map(inner -> inner.mapValue(mapper)));
    }
}
