package magma.core.result;

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
}
