package magma.result;

import magma.option.Option;

import java.util.function.Function;

public interface Result<T, X> {
    Option<T> findValue();

    Option<X> findErr();

    <R> Result<R, X> flatMapValue(Function<T, Result<R, X>> mapper);

    <R> Result<R, X> mapValue(Function<T, R> mapper);

    boolean isOk();
}
