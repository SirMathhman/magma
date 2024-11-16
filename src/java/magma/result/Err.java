package magma.result;

import magma.option.None;
import magma.option.Option;
import magma.option.Some;

public record Err<T, E>(E value) implements Result<T, E> {
    @Override
    public Option<E> findError() {
        return new Some<>(value);
    }

    @Override
    public Option<T> findValue() {
        return new None<>();
    }

    @Override
    public boolean isErr() {
        return true;
    }

    @Override
    public <R> Result<R, E> replaceValue(R value) {
        return new Err<R, E>(this.value);
    }

}
