package magma;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public record Ok<T, X>(T value) implements Result<T, X> {
    @Override
    public Optional<T> findValue() {
        return Optional.of(this.value);
    }

    @Override
    public Optional<X> findError() {
        return Optional.empty();
    }

    @Override
    public <R> Result<Tuple<T, R>, X> and(Supplier<Result<R, X>> otherSupplier) {
        return otherSupplier.get().mapValue(otherValue -> new Tuple<>(this.value, otherValue));
    }

    @Override
    public <R> Result<R, X> mapValue(Function<T, R> mapper) {
        return new Ok<>(mapper.apply(this.value));
    }

    @Override
    public <R> Result<R, X> flatMapValue(Function<T, Result<R, X>> mapper) {
        return mapper.apply(this.value);
    }
}
