package magma.api.result;

import magma.api.Tuple;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

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

    @Override
    public boolean isErr() {
        return true;
    }

    @Override
    public <R> Result<Tuple<T, R>, E> and(Supplier<Result<R, E>> supplier) {
        return new Err<>(error);
    }

    @Override
    public boolean isOk() {
        return false;
    }
}
