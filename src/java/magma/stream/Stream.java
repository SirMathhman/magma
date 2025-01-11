package magma.stream;

import magma.option.Option;

import java.util.function.BiFunction;
import java.util.function.Function;

public interface Stream<T> {
    <R> Stream<R> map(Function<T, R> mapper);

    <R> Stream<R> flatMap(Function<T, Stream<R>> mapper);

    <R> R foldLeft(R initial, BiFunction<R, T, R> folder);

    Option<T> next();

    Stream<T> concat(Stream<T> other);
}
