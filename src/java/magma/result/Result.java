package magma.result;

import magma.option.Option;

import java.util.function.Function;

public interface Result<T, E> {
    <R> Result<R, E> mapValue(Function<T, R> mapper);

    <R> R match(Function<T, R> onValid, Function<E, R> onError);

    Option<T> findValue();
}
