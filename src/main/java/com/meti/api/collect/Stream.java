package com.meti.api.collect;

import com.meti.api.core.F1;
import com.meti.api.core.F2;
import com.meti.api.option.Option;

public interface Stream<T> {
    default int count() throws StreamException {
        return foldRight(0, (current, value) -> current + 1);
    }

    <R, E extends Exception> R foldRight(R identity, F2<R, T, R, E> folder) throws StreamException, E;

    Stream<T> filter(F1<T, Boolean, ?> predicate);

    Option<T> first() throws StreamException;

    <R> Stream<R> flatMap(F1<T, Stream<R>, ?> mapper) throws StreamException;

    <E extends Exception> Option<T> foldRight(F2<T, T, T, E> folder) throws StreamException, E;

    T head() throws StreamException;

    <R> Stream<R> map(F1<T, R, ?> mapper) throws StreamException;
}
