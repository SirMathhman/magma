package magma.stream;

import magma.result.Result;

import java.util.function.BiFunction;

public interface Stream<T> {
    <R, X> Result<R, X> foldLeftIntoResult(R initial, BiFunction<R, T, Result<R, X>> folder);
}
