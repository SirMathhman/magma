package magma.result;

import java.util.Optional;
import java.util.function.Function;

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
}
