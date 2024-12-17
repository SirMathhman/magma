package magma.api.stream;

import magma.api.collect.Collector;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public interface Stream<T> extends Head<T> {
    <R> R foldLeft(R initial, BiFunction<R, T, R> folder);

    <R> R into(Function<Stream<T>, R> mapper);

    <R> Stream<R> map(Function<T, R> mapper);

    <R> Stream<R> flatMap(Function<T, Head<R>> mapper);

    Stream<T> filter(Predicate<T> predicate);

    <C> C collect(Collector<T, C> collector);
}
