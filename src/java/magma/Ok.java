package magma;

import java.util.ArrayList;
import java.util.List;

public record Ok<T, X>(T value) implements Result<T, X> {
    @Override
    public boolean isErr() {
        return false;
    }

    @Override
    public Option<X> findErr() {
        return new None<>();
    }

    @Override
    public Option<T> findValue() {
        return new Some<>(value);
    }
}
