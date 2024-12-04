package magma.option;

import magma.Tuple;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Option<T> {
    boolean isPresent();

    T orElseNull();

    <R> Option<R> map(Function<T, R> mapper);

    Tuple<Boolean, T> toTuple(T other);

    void ifPresent(Consumer<T> consumer);

    Option<T> or(Supplier<Option<T>> other);

    T orElseGet(Supplier<T> other);
}
