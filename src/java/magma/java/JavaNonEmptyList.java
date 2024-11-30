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
import java.util.function.Function;
import java.util.stream.Collectors;

public record JavaNonEmptyList<T>(List<T> list) {
    public JavaNonEmptyList(T initial) {
        this(new ArrayList<>(Collections.singletonList(initial)));
    }

    public static <T> Option<JavaNonEmptyList<T>> from(JavaList<T> list) {
        return list.isEmpty()
                ? new Some<>(new JavaNonEmptyList<>(list.list()))
                : new None<>();
    }

    @Override
    public String toString() {
        return list.stream()
                .map(Objects::toString)
                .collect(Collectors.joining(", ", "[", "]"));
    }

    public T last() {
        return list.getLast();
    }

    public JavaNonEmptyList<T> mapLast(Function<T, T> mapper) {
        final var last = list.getLast();
        final var newLast = mapper.apply(last);
        list.set(list.size() - 1, newLast);
        return this;
    }

    public Stream<Tuple<Integer, T>> streamReverseWithIndices() {
        return new HeadedStream<>(new RangeHead(list.size()))
                .extend(index -> list.get(list.size() - index - 1));
    }

    public Option<T> get(int index) {
        return index < list.size()
                ? new Some<>(list.get(index))
                : new None<>();
    }

    public Option<Stream<T>> sliceTo(int extent) {
        if (extent < list.size()) return new None<>();
        return new Some<>(new HeadedStream<>(new RangeHead(extent))
                .map(list::get));
    }

    public T first() {
        if (list().isEmpty()) throw new IllegalStateException();
        return list.getFirst();
    }

    public Tuple<T, JavaList<T>> popFirst() {
        return new Tuple<>(first(), new JavaList<>(list.subList(1, list.size())));
    }

    public Stream<T> stream() {
        return new HeadedStream<>(new RangeHead(list.size()))
                .map(list::get);
    }
}
