package magma.option;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface Option<T> {
    boolean isPresent();

    T orElseNull();

    <R> Option<R> map(Function<T, R> mapper);

    T orElseGet(Supplier<T> other);

    void ifPresent(Consumer<T> consumer);

    Option<T> or(Supplier<Option<T>> other);

    <R> R match(Function<T, R> ifPresent, Supplier<R> ifEmpty);

    Option<T> filter(Predicate<T> predicate);
}
