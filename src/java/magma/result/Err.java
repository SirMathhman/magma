package magma.result;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public record Err<T, E>(E error) implements Result<T, E> {
    @Override
    public Optional<T> findValue() {
        return Optional.empty();
    }

    @Override
    public <R> Result<Tuple<T, R>, E> and(Supplier<Result<R, E>> other) {
        return new Err<>(error);
    }

    @Override
    public <R> Result<R, E> mapValue(Function<T, R> mapper) {
        return new Err<>(error);
    }

    @Override
    public <R> Result<R, E> flatMapValue(Function<T, Result<R, E>> mapper) {
        return new Err<>(error);
    }

    @Override
    public Optional<E> findError() {
        return Optional.of(error);
    }
}