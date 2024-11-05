package magma.api.stream;

import java.util.function.BiFunction;
import java.util.function.Function;

public interface Stream<T> {
    <R> Stream<R> map(Function<T, R> mapper);

    <R> R into(Function<Stream<T>, R> mapper);

    <R> R foldLeft(R initial, BiFunction<R, T, R> folder);
}
