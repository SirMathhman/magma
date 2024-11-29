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
import java.util.function.Function;

public record JavaNonEmptyList<T>(List<T> list) {
    public JavaNonEmptyList(T initial) {
        this(new ArrayList<>(Collections.singletonList(initial)));
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
        if(extent < list.size()) return new None<>();
        return new Some<>(new HeadedStream<>(new RangeHead(extent))
                .map(list::get));
    }
}
