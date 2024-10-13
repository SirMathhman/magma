package magma.result;

import java.util.Optional;
import java.util.function.Function;

public class Err<T, E> implements Result<T, E> {
    private final E error;

    public Err(E error) {
        this.error = error;
    }

    @Override
    public Optional<T> findValue() {
        return Optional.empty();
    }

    @Override
    public Optional<E> findError() {
        return Optional.of(error);
    }

    @Override
    public <R> Result<R, E> mapValue(Function<T, R> mapper) {
        return new Err<>(error);
    }
}
