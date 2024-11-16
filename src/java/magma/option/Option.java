package magma.option;

import java.util.function.Supplier;

public interface Option<T> {
    boolean isPresent();

    T orElseGet(Supplier<T> other);

    boolean isEmpty();

    T orElse(T other);
}
