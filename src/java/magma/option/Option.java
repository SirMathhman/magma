package magma.option;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Option<T> {
    boolean isPresent();

    void ifPresent(Consumer<T> consumer);

    T orElse(T other);

    boolean isEmpty();

    <R> Option<R> map(Function<T, R> mapper);

    T orElseGet(Supplier<T> other);
}
