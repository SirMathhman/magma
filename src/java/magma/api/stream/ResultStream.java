package magma.api.stream;

import magma.api.collect.Collector;
import magma.api.option.Option;
import magma.api.result.Result;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public record ResultStream<T, X>(Stream<Result<T, X>> stream) implements Stream<Result<T, X>> {
    @Override
    public <R> R foldLeft(R initial, BiFunction<R, Result<T, X>, R> folder) {
        return stream.foldLeft(initial, folder);
    }

    @Override
    public <C> C collect(Collector<Result<T, X>, C> collector) {
        return stream.collect(collector);
    }

    @Override
    public <R> R into(Function<Stream<Result<T, X>>, R> mapper) {
        return stream.into(mapper);
    }

    @Override
    public <R> Stream<R> map(Function<Result<T, X>, R> mapper) {
        return stream.map(mapper);
    }

    @Override
    public Stream<Result<T, X>> filter(Predicate<Result<T, X>> predicate) {
        return stream.filter(predicate);
    }

    @Override
    public Option<Result<T, X>> next() {
        return stream.next();
    }
}
