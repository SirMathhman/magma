package magma.api.result;

import magma.api.Tuple;
import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;

import java.util.function.Function;
import java.util.function.Supplier;

public record Err<T, X>(X error) implements Result<T, X> {
    @Override
    public Option<T> findValue() {
        return new None<>();
    }

    @Override
    public Option<X> findError() {
        return new Some<>(error);
    }

    @Override
    public <R> Result<R, X> mapValue(Function<T, R> mapper) {
        return new Err<>(error);
    }

    @Override
    public <R> Result<R, X> flatMapValue(Function<T, Result<R, X>> mapper) {
        return new Err<>(error);
    }

    @Override
    public <R> Result<T, R> mapErr(Function<X, R> mapper) {
        return new Err<>(mapper.apply(error));
    }

    @Override
    public <R> R match(Function<T, R> onOk, Function<X, R> onErr) {
        return onErr.apply(error);
    }

    @Override
    public boolean isOk() {
        return false;
    }

    @Override
    public <R> Result<Tuple<T, R>, X> and(Supplier<Result<R, X>> other) {
        return new Err<>(error);
    }

}
