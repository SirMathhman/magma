package magma;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Function;

public interface Option<T> {
    boolean isPresent();

    void ifPresent(Consumer<T> consumer);

    T orElse(T other);

    boolean isEmpty();

    <R> Option<R> map(Function<T, R> mapper);
}
