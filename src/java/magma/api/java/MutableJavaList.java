package magma.api.java;

import magma.api.Tuple;
import magma.api.collect.Collector;
import magma.api.collect.List;
import magma.api.collect.RangeHead;
import magma.api.stream.HeadedStream;
import magma.api.stream.Stream;
import magma.api.stream.Streams;

import java.util.ArrayList;

public record MutableJavaList<T>(java.util.List<T> list) implements List<T> {
    public MutableJavaList() {
        this(new ArrayList<>());
    }

    public static <T> Collector<T, List<T>> collector() {
        return new ListCollector<>();
    }

    @Override
    public List<T> add(T other) {
        list.add(other);
        return this;
    }

    @Override
    public Stream<T> stream() {
        return Streams.from(list);
    }

    @Override
    public Stream<Tuple<Integer, T>> streamWithIndices() {
        return new HeadedStream<>(new RangeHead(list.size()))
                .map(index -> new Tuple<>(index, list.get(index)));
    }

    private static class ListCollector<T> implements Collector<T, List<T>> {
        @Override
        public List<T> createInitial() {
            return new MutableJavaList<>();
        }

        @Override
        public List<T> fold(List<T> current, T next) {
            return current.add(next);
        }
    }
}
