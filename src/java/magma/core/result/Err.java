package magma.core.result;

import magma.core.option.None;
import magma.core.option.Option;

import java.util.function.Consumer;
import java.util.function.Function;

public record Err<T, E>(E error) implements Result<T, E> {
    @Override
    public void consume(Consumer<T> onValid, Consumer<E> onInvalid) {
        onInvalid.accept(error);
    }

    @Override
    public <R> Result<R, E> mapValue(Function<T, R> mapper) {
        return new Err<>(error);
    }

    @Override
    public <R> R match(Function<T, R> onValid, Function<E, R> onInvalid) {
        return onInvalid.apply(error);
    }

    @Override
    public Option<T> findValue() {
        return new None<>();
    }

    @Override
    public <R> Result<T, R> mapErr(Function<E, R> mapper) {
        return new Err<>(mapper.apply(error));
    }

    @Override
    public <R> Result<R, E> flatMapValue(Function<T, Result<R, E>> mapper) {
        return new Err<>(error);
    }

    @Override
    public boolean isOk() {
        return false;
    }
}
