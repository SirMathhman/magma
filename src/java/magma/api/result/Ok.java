package magma.api.result;

import magma.api.Tuple;
import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;

import java.util.function.Function;
import java.util.function.Supplier;

public record Ok<T, E>(T value) implements Result<T, E> {
    @Override
    public <R> Result<R, E> mapValue(Function<T, R> mapper) {
        return new Ok<>(mapper.apply(value));
    }

    @Override
    public <R> R match(Function<T, R> onValid, Function<E, R> onError) {
        return onValid.apply(value);
    }

    @Override
    public Option<T> findValue() {
        return new Some<>(value);
    }

    @Override
    public <R> Result<R, E> flatMapValue(Function<T, Result<R, E>> mapper) {
        return mapper.apply(value);
    }

    @Override
    public <R> Result<T, R> mapErr(Function<E, R> mapper) {
        return new Ok<>(value);
    }

    @Override
    public <R> Result<Tuple<T, R>, E> and(Supplier<Result<R, E>> supplier) {
        return supplier.get().mapValue(other -> new Tuple<>(value, other));
    }

    @Override
    public boolean isErr() {
        return false;
    }

    @Override
    public Option<E> findErr() {
        return new None<>();
    }

    @Override
    public boolean isOk() {
        return true;
    }
}
