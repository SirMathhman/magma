package magma.result;

import java.util.function.Function;

public record Err<T, E>(E error) implements Result<T, E> {
    @Override
    public <R> Result<R, E> mapValue(Function<T, R> mapper) {
        return new Err<>(error);
    }

    @Override
    public <R> R match(Function<T, R> onValid, Function<E, R> onError) {
        return onError.apply(error);
    }
}

