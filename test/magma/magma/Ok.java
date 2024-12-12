package magma;

import java.util.Optional;

public record Ok<T, X>(T value) implements Result<T, X> {
    @Override
    public Optional<T> findValue() {
        return Optional.of(value);
    }

    @Override
    public boolean isErr() {
        return false;
    }
}
