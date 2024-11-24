package magma;

import java.util.function.Function;
import java.util.function.Supplier;

public interface Option<T> {
    <R> Option<R> map(Function<T, R> mapper);

    T orElseGet(Supplier<T> supplier);

    boolean isEmpty();

    <R> Option<Tuple<T, R>> and(Supplier<Option<R>> other);

    Option<T> or(Supplier<Option<T>> other);

    T orElse(T other);
}
