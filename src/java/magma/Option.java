package magma;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Option<T> {
    <R> R match(Function<T, R> ifPresent, Supplier<R> ifEmpty);

    void ifPresent(Consumer<T> consumer);
}
