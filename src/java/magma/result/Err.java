package magma.result;

import magma.Tuple;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class Err<T, X> implements Result<T, X> {
    private final X error;

    public Err(X error) {
        this.error = error;
    }

    @Override
    public <R> Result<R, X> mapValue(Function<T, R> mapper) {
        return new Err<>(this.error);
    }

    @Override
    public <R> R match(Function<T, R> onOk, Function<X, R> onErr) {
        return onErr.apply(this.error);
    }

    @Override
    public <R> Result<R, X> flatMapValue(Function<T, Result<R, X>> mapper) {
        return new Err<>(this.error);
    }

    @Override
    public Optional<T> findValue() {
        return Optional.empty();
    }

    @Override
    public boolean isOk() {
        return false;
    }

    @Override
    public <R> Result<Tuple<T, R>, X> and(Supplier<Result<R, X>> other) {
        return new Err<>(this.error);
    }

    @Override
    public <R> Result<T, R> mapErr(Function<X, R> mapper) {
        return new Err<>(mapper.apply(this.error));
    }

    @Override
    public Optional<X> findError() {
        return Optional.of(this.error);
    }
}
