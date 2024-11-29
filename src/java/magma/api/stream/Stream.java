package magma.api.stream;

import magma.api.option.Option;

import java.util.function.BiFunction;
import java.util.function.Function;

public interface Stream<T> {
    <R> Stream<R> map(Function<T, R> mapper);

    <R> R into(Function<Stream<T>, R> mapper);

    <C> C foldLeft(C initial, BiFunction<C, T, C> folder);

    Option<T> foldLeft(BiFunction<T, T, T> folder);
}