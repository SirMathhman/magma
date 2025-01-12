package magma.stream;

import magma.Tuple;
import magma.option.Option;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public interface Stream<T> {
    <R> Stream<R> map(Function<T, R> mapper);

    <R> Stream<R> flatMap(Function<T, Stream<R>> mapper);

    <R> R foldLeft(R initial, BiFunction<R, T, R> folder);

    <R> Option<R> foldLeftWithInit(Function<T, R> initial, BiFunction<R, T, R> folder);

    Option<T> next();

    Stream<T> concat(Stream<T> other);

    Stream<T> filter(Predicate<T> predicate);

    <C> C collect(Collector<T, C> collector);

    <R> Stream<Tuple<T, R>> extendBy(Function<T, R> mapper);
}
