package magma.result;

import magma.Tuple;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class Ok<T, X> implements Result<T, X> {
    private final T value;

    public Ok(T value) {
        this.value = value;
    }

    @Override
    public <R> Result<R, X> mapValue(Function<T, R> mapper) {
        return new Ok<>(mapper.apply(this.value));
    }

    @Override
    public <R> R match(Function<T, R> onOk, Function<X, R> onErr) {
        return onOk.apply(this.value);
    }

    @Override
    public <R> Result<R, X> flatMapValue(Function<T, Result<R, X>> mapper) {
        return mapper.apply(this.value);
    }

    @Override
    public Optional<T> findValue() {
        return Optional.of(this.value);
    }

    @Override
    public boolean isOk() {
        return true;
    }

    @Override
    public <R> Result<Tuple<T, R>, X> and(Supplier<Result<R, X>> other) {
        return other.get().mapValue(otherValue -> new Tuple<>(this.value, otherValue));
    }

    @Override
    public <R> Result<T, R> mapErr(Function<X, R> mapper) {
        return new Ok<>(this.value);
    }

    @Override
    public Optional<X> findError() {
        return Optional.empty();
    }
}
