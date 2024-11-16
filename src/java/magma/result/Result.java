package magma.result;

import magma.option.Option;

public interface Result<T, E> {
    Option<E> findError();

    Option<T> findValue();

    boolean isErr();

    <R> Result<R, E> replaceValue(R value);
}
