package magma.stream;

import magma.option.Option;
import magma.result.Result;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public interface Stream<T> {
    <R, X> Result<R, X> foldLeftIntoResult(R initial, BiFunction<R, T, Result<R, X>> folder);

    <R> Stream<R> map(Function<T, R> mapper);

    <R> Stream<R> flatMap(Function<T, Stream<R>> mapper);

    Option<T> next();

    Stream<T> concat(Stream<T> other);

    <R> R foldLeft(R initial, BiFunction<R, T, R> folder);

    Stream<T> filter(Predicate<T> predicate);
}
