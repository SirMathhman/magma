package magma.java;

import magma.stream.Collector;
import magma.stream.HeadedStream;
import magma.stream.Stream;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public record JavaSet<T>(Set<T> internal) implements magma.collect.Set<T> {
    public JavaSet() {
        this(new HashSet<>());
    }

    public static <T> Collector<T, magma.collect.Set<T>> collector() {
        return new SetCollector<>();
    }

    @Override
    public Stream<T> stream() {
        return new HeadedStream<>(new JavaListHead<>(new ArrayList<>(this.internal)));
    }

    private static class SetCollector<T> implements Collector<T, magma.collect.Set<T>> {
        @Override
        public magma.collect.Set<T> createInitial() {
            return new JavaSet<>();
        }

        @Override
        public magma.collect.Set<T> fold(magma.collect.Set<T> current, T next) {
            return current.add(next);
        }
    }
}