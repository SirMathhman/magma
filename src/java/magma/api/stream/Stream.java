package magma.api.stream;

import java.util.function.BiFunction;
import java.util.function.Function;

public interface Stream<T> {
    <R> R foldLeft(R initial, BiFunction<R, T, R> folder);

    <R> R into(Function<Stream<T>, R> mapper);
}
