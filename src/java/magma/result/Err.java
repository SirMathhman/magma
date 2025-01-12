package magma.result;

import magma.option.None;
import magma.option.Option;

import java.util.function.Function;

public record Err<T, X>(X error) implements Result<T, X> {
    @Override
    public <R> R match(Function<T, R> valueMapper, Function<X, R> errorMapper) {
        return errorMapper.apply(this.error);
    }

    @Override
    public <R> Result<R, X> mapValue(Function<T, R> mapper) {
        return new Err<>(this.error);
    }

    @Override
    public Option<T> findValue() {
        return new None<>();
    }

    @Override
    public <R> Result<R, X> flatMapValue(Function<T, Result<R, X>> mapper) {
        return new Err<>(this.error);
    }
}
