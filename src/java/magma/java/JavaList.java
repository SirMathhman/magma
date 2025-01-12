package magma.java;

import magma.option.None;
import magma.option.Option;
import magma.option.Some;
import magma.stream.Collector;
import magma.stream.HeadedStream;
import magma.stream.Stream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class JavaList<T> implements magma.collect.List<T> {
    private final List<T> internal;

    public JavaList() {
        this(new ArrayList<>());
    }

    public JavaList(List<T> internal) {
        this.internal = internal;
    }

    public static <T> Collector<T, magma.collect.List<T>> collector() {
        return new ListCollector<>();
    }

    @SafeVarargs
    public static <T> magma.collect.List<T> of(T... values) {
        return new JavaList<>(Arrays.asList(values));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JavaList<?> javaList = (JavaList<?>) o;
        return Objects.equals(this.internal, javaList.internal);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.internal);
    }

    @Override
    public JavaList<T> add(T next) {
        this.internal.add(next);
        return this;
    }

    @Override
    public int size() {
        return this.internal.size();
    }

    @Override
    public Option<magma.collect.List<T>> slice(int start, int end) {
        if (start >= 0 && end >= 0 && start <= size() && end <= size() && start <= end) {
            return new Some<>(new JavaList<>(this.internal.subList(start, end)));
        }

        return new None<>();
    }

    @Override
    public Stream<T> stream() {
        return new HeadedStream<>(new JavaListHead<>(this.internal));
    }

    private static class ListCollector<T> implements Collector<T, magma.collect.List<T>> {
        @Override
        public magma.collect.List<T> createInitial() {
            return new JavaList<>();
        }

        @Override
        public magma.collect.List<T> fold(magma.collect.List<T> current, T next) {
            return current.add(next);
        }
    }
}
