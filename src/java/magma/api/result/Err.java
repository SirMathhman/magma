package magma.api.result;

import magma.api.Tuple;
import magma.api.option.None;
import magma.api.option.Option;

import java.util.function.Function;
import java.util.function.Supplier;

public record Err<T, E>(E value) implements Result<T, E> {
    @Override
    public <R> Result<R, E> mapValue(Function<T, R> mapper) {
        return new Err<>(value);
    }

    @Override
    public <R> R match(Function<T, R> onValid, Function<E, R> onError) {
        return onError.apply(value);
    }

    @Override
    public Option<T> findValue() {
        return new None<>();
    }

    @Override
    public <R> Result<R, E> flatMapValue(Function<T, Result<R, E>> mapper) {
        return new Err<>(value);
    }

    @Override
    public <R> Result<T, R> mapErr(Function<E, R> mapper) {
        return new Err<>(mapper.apply(value));
    }

    @Override
    public <R> Result<Tuple<T, R>, E> and(Supplier<Result<R, E>> supplier) {
        return new Err<>(value);
    }
}

