package magma;

import java.util.ArrayList;
import java.util.function.Consumer;

public interface Option<T> {
    boolean isPresent();

    void ifPresent(Consumer<T> consumer);

    T orElse(T other);

    boolean isEmpty();
}
