package magma.result;

import java.util.function.Function;

public record Ok<T, E>(T value) implements Result<T, E> {
    @Override
    public <R> Result<R, E> mapValue(Function<T, R> mapper) {
        return new Ok<>(mapper.apply(value));
    }

    @Override
    public <R> R match(Function<T, R> onValid, Function<E, R> onError) {
        return onValid.apply(value);
    }
}
