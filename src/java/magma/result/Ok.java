package magma.result;

import java.util.function.Function;

public record Ok<T, X>(T value) implements Result<T, X> {
    @Override
    public <R> Result<R, X> flatMapValue(Function<T, Result<R, X>> mapper) {
        return mapper.apply(this.value);
    }

    @Override
    public <R> Result<R, X> mapValue(Function<T, R> mapper) {
        return new Ok<>(mapper.apply(this.value));
    }

    @Override
    public <R> Result<T, R> mapErr(Function<X, R> mapper) {
        return new Ok<>(this.value);
    }

    @Override
    public <R> R match(Function<T, R> onOk, Function<X, R> onErr) {
        return onOk.apply(this.value);
    }
}
