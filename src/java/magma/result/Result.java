package magma.result;

import magma.option.Option;

import java.util.function.Function;

public interface Result<T, X> {
    <R> R match(Function<T, R> valueMapper, Function<X, R> errorMapper);

    <R> Result<R, X> mapValue(Function<T, R> mapper);

    Option<T> findValue();

    <R> Result<R, X> flatMapValue(Function<T, Result<R, X>> mapper);
}
