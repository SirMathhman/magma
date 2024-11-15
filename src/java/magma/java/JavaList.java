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
import java.util.function.Function;

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

    public Option<JavaList<T>> popLastAndDrop() {
        if (list.isEmpty()) return new None<>();
        final var slice = list.subList(0, list.size() - 1);
        return new Some<>(new JavaList<>(slice));
    }

    public Option<JavaList<T>> mapLast(Function<T, T> mapper) {
        if (!list.isEmpty()) return new None<>();

        final var copy = new ArrayList<>(list);
        final var lastIndex = copy.size() - 1;
        final var last = copy.get(lastIndex);
        final var newLast = mapper.apply(last);
        copy.set(lastIndex, newLast);
        return new Some<>(new JavaList<>(copy));
    }

    public Option<T> findLast() {
        if (list.isEmpty()) return new None<>();
        return new Some<>(list.getLast());
    }

    public JavaList<T> setLast(T last) {
        if (list.isEmpty()) return new JavaList<>();

        final var copy = new ArrayList<>(list);
        copy.set(copy.size() - 1, last);
        return new JavaList<>(copy);
    }
}