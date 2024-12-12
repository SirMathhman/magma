package magma;

import java.util.Optional;

public record Err<T, X>(X error) implements Result<T, X> {
    @Override
    public Optional<T> findValue() {
        return Optional.empty();
    }

    @Override
    public boolean isErr() {
        return true;
    }
}
