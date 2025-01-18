package magma.result;

import java.util.function.Function;

public record Err<T, X>(X error) implements Result<T, X> {
    @Override
    public <R> Result<R, X> flatMapValue(Function<T, Result<R, X>> mapper) {
        return new Err<>(this.error);
    }

    @Override
    public <R> Result<R, X> mapValue(Function<T, R> mapper) {
        return new Err<>(this.error);
    }

    @Override
    public <R> Result<T, R> mapErr(Function<X, R> mapper) {
        return new Err<>(mapper.apply(this.error));
    }

    @Override
    public <R> R match(Function<T, R> onOk, Function<X, R> onErr) {
        return onErr.apply(this.error);
    }
}
