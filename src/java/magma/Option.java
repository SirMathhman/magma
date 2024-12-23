package magma;

import java.util.function.Supplier;

public interface Option<T> {
    T orElseGet(Supplier<T> other);
}
