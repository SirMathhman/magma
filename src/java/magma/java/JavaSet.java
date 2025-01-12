package magma.java;

import magma.stream.Collector;
import magma.stream.HeadedStream;
import magma.stream.Stream;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public record JavaSet<T>(Set<T> internal) {
    public JavaSet() {
        this(new HashSet<>());
    }

    public static <T> Collector<T, JavaSet<T>> collector() {
        return new SetCollector<>();
    }

    private JavaSet<T> add(T next) {
        this.internal.add(next);
        return this;
    }

    public Stream<T> stream() {
        return new HeadedStream<>(new JavaListHead<>(new ArrayList<>(this.internal)));
    }

    private static class SetCollector<T> implements Collector<T, JavaSet<T>> {
        @Override
        public JavaSet<T> createInitial() {
            return new JavaSet<>();
        }

        @Override
        public JavaSet<T> fold(JavaSet<T> current, T next) {
            return current.add(next);
        }
    }
}