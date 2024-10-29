package magma;

import java.util.function.Consumer;
import java.util.function.Function;

public interface Option<T> {
    <R> Option<R> map(Function<T, R> mapper);

    T orElse(T other);

    void ifPresent(Consumer<T> consumer);
}
