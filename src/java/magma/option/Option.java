package magma.option;

import java.util.function.Function;
import java.util.function.Supplier;

public interface Option<T> {
    Option<T> or(Supplier<Option<T>> other);

    <R> Option<R> map(Function<T, R> mapper);

    T orElseGet(Supplier<T> other);

    boolean isPresent();

    T orElseNull();
}
