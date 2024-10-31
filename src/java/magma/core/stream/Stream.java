package magma.core.stream;

import java.util.function.Function;
import java.util.function.Predicate;

public interface Stream<T> extends Head<T> {
    <R> Stream<R> map(Function<T, R> mapper);

    Stream<T> filter(Predicate<T> predicate);
}
