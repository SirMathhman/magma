package magma.result;

import magma.option.None;
import magma.option.Option;
import magma.option.Some;

public record Ok<T, X>(T value) implements Result<T, X> {
    @Override
    public Option<T> findValue() {
        return new Some<>(value);
    }

    @Override
    public Option<X> findErr() {
        return new None<>();
    }
}
