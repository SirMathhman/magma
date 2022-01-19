package com.meti.api.collect.java;

import com.meti.api.collect.stream.AbstractStream;
import com.meti.api.collect.stream.EndOfStreamException;
import com.meti.api.collect.stream.Stream;
import com.meti.api.collect.stream.StreamException;
import com.meti.api.core.F1;
import com.meti.api.option.None;
import com.meti.api.option.Option;
import com.meti.api.option.Some;
import com.meti.api.option.Supplier;

import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;

public class JavaList<T> implements List<T> {
    private final java.util.List<T> value;

    JavaList() {
        this(new ArrayList<>());
    }

    JavaList(java.util.List<T> value) {
        this.value = value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JavaList<?> list = (JavaList<?>) o;
        return Objects.equals(value, list.value);
    }

    @Override
    public String toString() {
        return value.stream()
                .map(Objects::toString)
                .collect(Collectors.joining(",", "[", "]"));
    }

    private class JavaListStream extends AbstractStream<T> {
        private int counter = 0;

        @Override
        public T head() throws StreamException {
            if (counter < size()) {
                return value.get(counter++);
            } else {
                throw new EndOfStreamException("No more elements left in list.");
            }
        }
    }

    @Override
    public List<T> add(T node) {
        value.add(node);
        return this;
    }

    @Override
    public List<T> addAll(List<T> others) {
        try {
            return others.stream().<List<T>, RuntimeException>foldRight(this, List::add);
        } catch (StreamException e) {
            return new JavaList<>();
        }
    }

    @Override
    public T apply(int index) {
        return value.get(index);
    }

    @Override
    public boolean contains(T element) {
        return value.contains(element);
    }

    @Override
    public <E extends Exception> List<T> ensure(Supplier<T, E> supplier) throws E {
        if (value.isEmpty()) value.add(supplier.get());
        return this;
    }

    @Override
    public Option<T> first() {
        return value.isEmpty() ? new None<>() : new Some<>(value.get(0));
    }


    @Override
    public List<T> insert(int index, T value) {
        this.value.add(index, value);
        return this;
    }

    @Override
    public boolean isEmpty() {
        return value.isEmpty();
    }

    @Override
    public Option<T> last() {
        if (value.isEmpty()) return new None<>();
        else return new Some<>(value.get(value.size() - 1));
    }

    @Override
    public <E extends Exception> List<T> mapLast(F1<T, T, E> mapper) throws E {
        if (!isEmpty()) {
            var lastIndex = value.size() - 1;
            var last = value.get(lastIndex);
            var mapped = mapper.apply(last);
            value.set(lastIndex, mapped);
        }
        return this;
    }

    @Override
    public int size() {
        return value.size();
    }

    @Override
    public Stream<T> stream() {
        return new JavaListStream();
    }


}
