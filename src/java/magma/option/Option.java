package magma.option;

import magma.Tuple;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface Option<T> {
    <R> R match(Function<T, R> ifPresent, Supplier<R> ifEmpty);

    void ifPresent(Consumer<T> consumer);

    <R> Option<R> map(Function<T, R> mapper);

    T orElseGet(Supplier<T> other);

    Tuple<Boolean, T> toTuple(T other);

    Option<T> or(Supplier<Option<T>> other);

    boolean isEmpty();

    boolean isPresent();

    @Deprecated
    T unwrap();

    T orElse(T other);

    <R> Option<R> flatMap(Function<T, Option<R>> mapper);

    Option<T> filter(Predicate<T> predicate);
}
