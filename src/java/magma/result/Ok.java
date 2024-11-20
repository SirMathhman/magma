package magma.result;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public record Ok<T, E>(T value) implements Result<T, E> {
    @Override
    public Optional<T> findValue() {
        return Optional.of(value);
    }

    @Override
    public <R> Result<Tuple<T, R>, E> and(Supplier<Result<R, E>> other) {
        return other.get().mapValue(otherValue -> new Tuple<>(value, otherValue));
    }

    @Override
    public <R> Result<R, E> mapValue(Function<T, R> mapper) {
        return new Ok<>(mapper.apply(value));
    }

    @Override
    public <R> Result<R, E> flatMapValue(Function<T, Result<R, E>> mapper) {
        return mapper.apply(value);
    }

    @Override
    public Optional<E> findError() {
        return Optional.empty();
    }
}
