package magma.api.option;

import magma.api.Tuple;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Option<T> {
    void ifPresent(Consumer<T> consumer);

    <R> Option<R> map(Function<T, R> mapper);

    Option<T> or(Supplier<Option<T>> other);

    T orElseGet(Supplier<T> other);

    Tuple<Boolean, T> toTuple(T other);
}
