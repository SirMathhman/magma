package com.meti.java;

import com.meti.core.Option;
import com.meti.iterate.Index;
import com.meti.iterate.Iterator;

import java.util.Comparator;
import java.util.function.Function;

public interface List<T> {
    List<T> add(T element);

    Iterator<T> iter();

    Option<Index> lastIndexOptionally();

    List<T> sliceTo(Index extent);

    boolean isEmpty();

    <R> R into(Function<List<T>, R> mapper);

    java.util.List<T> unwrap();

    Index size();

    List<T> addAll(JavaList<T> others);

    List<T> sort(Comparator<T> comparator);
}
