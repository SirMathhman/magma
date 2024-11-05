package magma.api.option;

import magma.api.Tuple;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Option<T> {
    void ifPresent(Consumer<T> consumer);

    T orElse(T other);

    <R> Option<R> map(Function<T, R> mapper);

    <R> Option<R> flatMap(Function<T, Option<R>> mapper);

    T orElseGet(Supplier<T> supplier);

    boolean isPresent();

    Option<T> or(Supplier<Option<T>> supplier);

    Tuple<Boolean, T> toTuple(T other);
}
