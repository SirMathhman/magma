package magma.result;

import java.util.Optional;

public record Err<T, E>(E e) implements Result<T, E> {
    @Override
    public Optional<T> findValue() {
        return Optional.empty();
    }
}
