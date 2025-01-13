package magma.result;

import java.util.function.Function;

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
}
