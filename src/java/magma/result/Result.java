package magma.result;

import java.util.function.Function;

public interface Result<T, X> {
    <R> R match(Function<T, R> valueMapper, Function<X, R> errorMapper);

    <R> Result<R, X> mapValue(Function<T, R> mapper);
}
