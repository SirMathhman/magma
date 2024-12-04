package magma.api.result;

import magma.api.option.Option;

import java.util.function.Function;

public interface Result<T, X> {
    Option<T> findValue();

    Option<X> findError();

    <R> Result<R, X> mapValue(Function<T, R> mapper);

    <R> Result<R, X> flatMapValue(Function<T, Result<R, X>> mapper);

    <R> Result<T, R> mapErr(Function<X, R> mapper);

    <R> R match(Function<T, R> onOk, Function<X, R> onErr);

    boolean isOk();
}
