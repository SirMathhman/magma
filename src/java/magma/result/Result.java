package magma.result;

import java.util.function.Consumer;
import java.util.function.Function;

public interface Result<T, E> {
    void consume(Consumer<T> onValid, Consumer<E> onInvalid);

    <R> Result<R, E> mapValue(Function<T, R> mapper);

    <R> R match(Function<T, R> onValid, Function<E, R> onInvalid);
}
