package com.meti;

import java.util.function.Function;
import java.util.function.Predicate;

public interface Stream<T> {
    Option<T> next();

    <R> Stream<R> map(Function<T, R> mapper);

    Stream<T> filter(Predicate<T> predicate);

    <R> Stream<R> flatMap(Function<T, Stream<R>> mapper);

    <C> C collect(Collector<T, C> collector);
}
