package magma.api.stream;

import magma.api.option.Option;
import magma.api.result.Result;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public interface Stream<T> {
    static <T> Stream<T> fromOption(Option<T> option) {
        throw new UnsupportedOperationException();
    }

    <R> Stream<R> map(Function<T, R> mapper);

    <R> R into(Function<Stream<T>, R> mapper);

    <R> R foldLeft(R initial, BiFunction<R, T, R> folder);

    boolean allMatch(Predicate<T> predicate);

    <R, E> Result<R, E> foldLeftToResult(R initial, BiFunction<R, T, Result<R, E>> folder);

    <R> Stream<R> flatMap(Function<T, Stream<R>> mapper);

    Option<T> next();

    Stream<T> concat(Stream<T> other);
}
