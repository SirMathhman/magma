package magma.api.stream;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface Stream<T> {
    <R> Optional<R> foldLeft(Function<T, R> initialMapper, BiFunction<R, T, R> folder);

    <R> Stream<R> map(Function<T, R> mapper);
}