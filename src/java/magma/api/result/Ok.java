package magma.api.result;

import magma.api.Tuple;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class Ok<T, E> implements Result<T, E> {
    private final T value;

    public Ok(T value) {
        this.value = value;
    }

    @Override
    public Optional<T> findValue() {
        return Optional.of(value);
    }

    @Override
    public Optional<E> findError() {
        return Optional.empty();
    }

    @Override
    public <R> Result<R, E> mapValue(Function<T, R> mapper) {
        return new Ok<>(mapper.apply(value));
    }

    @Override
    public boolean isErr() {
        return false;
    }

    @Override
    public <R> Result<Tuple<T, R>, E> and(Supplier<Result<R, E>> supplier) {
        return supplier.get().mapValue(inner -> new Tuple<>(value, inner));
    }

    @Override
    public boolean isOk() {
        return true;
    }

    @Override
    public <R> Result<R, E> flatMapValue(Function<T, Result<R, E>> mapper) {
        return mapper.apply(value);
    }

    @Override
    public <R> Result<T, R> mapErr(Function<E, R> mapper) {
        return new Ok<>(value);
    }

    @Override
    public <R> R match(Function<T, R> onValid, Function<E, R> onError) {
        return onValid.apply(value);
    }
}
