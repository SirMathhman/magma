package magma.api.stream;

import magma.api.Tuple;
import magma.api.option.Option;
import magma.api.result.Ok;
import magma.api.result.Result;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public record ResultStream<T, X>(Stream<Result<T, X>> parent) implements Stream<Result<T, X>> {
    @Override
    public <R> Stream<R> map(Function<Result<T, X>, R> mapper) {
        return parent.map(mapper);
    }

    @Override
    public <R> Stream<R> flatMap(Function<Result<T, X>, Stream<R>> mapper) {
        return parent.flatMap(mapper);
    }

    @Override
    public <R> Stream<Tuple<Result<T, X>, R>> extend(Function<Result<T, X>, R> mapper) {
        return parent.extend(mapper);
    }

    @Override
    public Stream<Result<T, X>> filter(Predicate<Result<T, X>> predicate) {
        return parent.filter(predicate);
    }

    @Override
    public Option<Result<T, X>> next() {
        return parent.next();
    }

    @Override
    public Option<Result<T, X>> foldLeft(BiFunction<Result<T, X>, Result<T, X>, Result<T, X>> folder) {
        return parent.foldLeft(folder);
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
