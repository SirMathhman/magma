package magma;

import magma.result.Result;

import java.util.function.BiFunction;
import java.util.function.Function;

public record ResultStream<T, X>(Stream<Result<T, X>> stream) implements Stream<Result<T, X>> {
    @Override
    public <R> R foldLeft(R initial, BiFunction<R, Result<T, X>, R> folder) {
        return stream.foldLeft(initial, folder);
    }

    @Override
    public <R> R into(Function<Stream<Result<T, X>>, R> mapper) {
        return stream.into(mapper);
    }
}
