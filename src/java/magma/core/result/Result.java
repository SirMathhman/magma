package magma.core.result;

import magma.core.option.Option;

import java.util.function.Consumer;
import java.util.function.Function;

public interface Result<T, E> {
    void consume(Consumer<T> onValid, Consumer<E> onInvalid);

    <R> Result<R, E> mapValue(Function<T, R> mapper);

    <R> R match(Function<T, R> onValid, Function<E, R> onInvalid);

    Option<T> findValue();

    <R> Result<T, R> mapErr(Function<E, R> mapper);

    <R> Result<R, E> flatMapValue(Function<T, Result<R, E>> mapper);
}
