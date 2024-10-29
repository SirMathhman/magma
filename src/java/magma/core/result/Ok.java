package magma.core.result;

import java.util.function.Consumer;
import java.util.function.Function;

public record Ok<T, E>(T value) implements Result<T, E> {
    @Override
    public void consume(Consumer<T> onValid, Consumer<E> onInvalid) {
        onValid.accept(value);
    }

    @Override
    public <R> Result<R, E> mapValue(Function<T, R> mapper) {
        return new Ok<>(mapper.apply(value));
    }

    @Override
    public <R> R match(Function<T, R> onValid, Function<E, R> onInvalid) {
        return onValid.apply(value);
    }
}
