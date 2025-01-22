package magma.app;

import java.util.Optional;
import java.util.function.Predicate;

public interface PassUnit<T> {
    Optional<PassUnit<T>> filter(Predicate<T> predicate);

    <R> PassUnit<R> withValue(R value);

    PassUnit<T> enter();

    @Deprecated
    State state();

    @Deprecated
    T value();
}
