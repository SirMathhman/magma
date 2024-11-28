package magma.stream;

import magma.result.Ok;
import magma.result.Result;

import java.util.function.BiFunction;
import java.util.function.Function;

public interface Stream<T> {
    <R> Stream<R> map(Function<T, R> mapper);

    <R> R into(Function<Stream<T>, R> mapper);

    <C> C foldLeft(C initial, BiFunction<C, T, C> folder);
}
