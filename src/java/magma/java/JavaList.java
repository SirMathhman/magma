package magma.java;

import magma.option.None;
import magma.option.Option;
import magma.option.Some;
import magma.stream.Collector;
import magma.stream.HeadedStream;
import magma.stream.Stream;

import java.util.ArrayList;
import java.util.List;

public class JavaList<T> {
    private final List<T> internal;

    public JavaList() {
        this(new ArrayList<>());
    }

    public JavaList(List<T> internal) {
        this.internal = internal;
    }

    public static <T> Collector<T, JavaList<T>> collector() {
        return new ListCollector<>();
    }

    private JavaList<T> add(T next) {
        this.internal.add(next);
        return this;
    }

    public int size() {
        return this.internal.size();
    }

    public Option<JavaList<T>> subList(int start, int end) {
        if (start >= 0 && end >= 0 && start < size() && end < size() && end >= start) {
            return new Some<>(new JavaList<>(this.internal.subList(start, end)));
        }

        return new None<>();
    }

    public Stream<T> stream() {
        return new HeadedStream<>(new JavaListHead<>(this.internal));
    }

    private static class ListCollector<T> implements Collector<T, JavaList<T>> {
        @Override
        public JavaList<T> createInitial() {
            return new JavaList<>();
        }

        @Override
        public JavaList<T> fold(JavaList<T> current, T next) {
            return current.add(next);
        }
    }
}
