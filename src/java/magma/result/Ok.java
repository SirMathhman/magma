package magma.result;

import magma.option.Option;
import magma.option.Some;

import java.util.function.Function;

public record Ok<T, X>(T value) implements Result<T, X> {
    @Override
    public <R> R match(Function<T, R> valueMapper, Function<X, R> errorMapper) {
        return valueMapper.apply(this.value);
    }

    @Override
    public <R> Result<R, X> mapValue(Function<T, R> mapper) {
        return new Ok<>(mapper.apply(this.value));
    }

    @Override
    public Option<T> findValue() {
        return new Some<>(this.value);
    }

    @Override
    public <R> Result<R, X> flatMapValue(Function<T, Result<R, X>> mapper) {
        return mapper.apply(this.value);
    }
}
