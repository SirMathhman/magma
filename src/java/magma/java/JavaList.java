package magma.java;

import magma.api.Tuple;
import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.api.stream.HeadedStream;
import magma.api.stream.RangeHead;
import magma.api.stream.Stream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public record JavaList<T>(List<T> list) {
    public JavaList() {
        this(Collections.emptyList());
    }

    public JavaList<T> add(T node) {
        final var copy = new ArrayList<>(list);
        copy.add(node);
        return new JavaList<>(copy);
    }

    public Stream<T> stream() {
        return new HeadedStream<>(new RangeHead(list.size())).map(list::get);
    }

    public JavaList<T> addAll(JavaList<T> other) {
        final var copy = new ArrayList<>(list);
        copy.addAll(other.list);
        return new JavaList<>(copy);
    }

    public int size() {
        return list.size();
    }

    public Stream<Tuple<Integer, T>> streamWithIndices() {
        return new HeadedStream<>(new RangeHead(list.size())).extend(list::get);
    }

    public Option<Stream<T>> sliceTo(int index) {
        if (index < list.size()) {
            return new Some<>(new HeadedStream<>(new RangeHead(index))
                    .map(list::get));
        }
        return new None<>();
    }

    public Option<T> get(int index) {
        if (index < list.size()) return new Some<>(list.get(index));
        return new None<>();
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public Option<Tuple<T, JavaList<T>>> popFirst() {
        return isEmpty() ? new None<>() : new Some<>(new Tuple<>(list.getFirst(), new JavaList<>(list.subList(1, list.size()))));
    }

    @Override
    public String toString() {
        return list.stream()
                .map(Objects::toString)
                .collect(Collectors.joining(", ", "[", "]"));
    }

    public Option<Stream<T>> sliceFrom(int index) {
        if (index >= list.size()) return new None<>();

        return new Some<>(new HeadedStream<>(new RangeHead(list.size() - index))
                .map(offset -> index + offset)
                .map(list::get));
    }
}
