package magma.option;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Option<T> {
    <R> Option<R> map(Function<T, R> mapper);

    T orElseGet(Supplier<T> other);

    void ifPresent(Consumer<T> consumer);

    <R> Option<R> flatMapValue(Function<T, Option<R>> mapper);

    Option<T> or(Supplier<Option<T>> supplier);

    T orElse(T other);

    boolean isEmpty();
}
