package magma.result;

import magma.option.None;
import magma.option.Option;
import magma.option.Some;

public record Ok<T, E>(T value) implements Result<T, E> {
    @Override
    public Option<E> findError() {
        return new None<>();
    }

    @Override
    public Option<T> findValue() {
        return new Some<>(value);
    }

    @Override
    public boolean isErr() {
        return false;
    }

    @Override
    public <R> Result<R, E> replaceValue(R value) {
        return new Ok<>(value);
    }
}
