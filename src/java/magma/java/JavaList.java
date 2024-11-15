package magma.java;

import magma.api.Tuple;
import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.api.stream.HeadedStream;
import magma.api.stream.RangeStream;
import magma.api.stream.Stream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record JavaList<T>(List<T> list) {
    public JavaList() {
        this(Collections.emptyList());
    }

    public JavaList<T> addLast(T element) {
        var copy = new ArrayList<>(list());
        copy.add(element);
        return new JavaList<T>(copy);
    }

    public int size() {
        return list.size();
    }

    public Option<T> get(int index) {
        if (index >= 0 && index < list.size()) return new Some<>(list.get(index));
        else return new None<>();
    }

    public Stream<T> stream() {
        return new HeadedStream<>(new NativeListHead<>(list));
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public JavaList<T> addAll(JavaList<T> other) {
        final var copy = new ArrayList<>(list);
        copy.addAll(other.list);
        return new JavaList<>(copy);
    }

    public Stream<Tuple<Integer, T>> streamWithIndex() {
        return new HeadedStream<>(new RangeStream(list.size()))
                .map(index -> new Tuple<>(index, list.get(index)));
    }

    public Option<JavaList<T>> sliceToIndex(int index) {
        if (index < 0 || index >= list.size()) return new None<>();
        final var slice = list.subList(0, index);
        return new Some<>(new JavaList<>(slice));
    }

    public JavaList<T> addFirst(T element) {
        var copy = new ArrayList<T>();
        copy.add(element);
        copy.addAll(list);
        return new JavaList<T>(copy);
    }
}