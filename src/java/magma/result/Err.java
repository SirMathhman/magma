package magma.result;

import magma.option.None;
import magma.option.Option;
import magma.option.Some;

public record Err<T, E>(E e) implements Result<T, E> {
    @Override
    public Option<E> findError() {
        return new Some<>(e);
    }

    @Override
    public Option<T> findValue() {
        return new None<>();
    }
}
