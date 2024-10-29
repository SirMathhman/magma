package magma.option;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Option<T> {
    <R> Option<R> map(Function<T, R> mapper);

    T orElse(T other);

    void ifPresent(Consumer<T> consumer);

    Option<T> or(Supplier<Option<T>> other);

    <R> Option<R> flatMap(Function<T, Option<R>> mapper);
}
