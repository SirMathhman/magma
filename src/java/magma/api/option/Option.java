package magma.api.option;

import magma.api.Tuple;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface Option<T> {
    boolean isPresent();

    void ifPresent(Consumer<T> consumer);

    T orElse(T other);

    boolean isEmpty();

    <R> Option<R> map(Function<T, R> mapper);

    T orElseGet(Supplier<T> other);

    Option<T> filter(Predicate<T> predicate);

    <R> Option<R> flatMap(Function<T, Option<R>> mapper);

    Tuple<Boolean, T> toTuple(T other);

    Option<T> or(Supplier<Option<T>> other);
}
