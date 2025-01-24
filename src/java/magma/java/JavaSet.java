package magma.java;

import magma.api.stream.Collector;
import magma.api.stream.Stream;
import magma.api.stream.Streams;

import java.util.HashSet;
import java.util.Set;

public record JavaSet<T>(Set<T> set) {
    public JavaSet() {
        this(new HashSet<>());
    }

    public static <T> Collector<T, JavaSet<T>> collect() {
        return new JavaSetCollector<>();
    }

    public JavaSet<T> add(T element) {
        final var copy = new HashSet<>(this.set);
        copy.add(element);
        return new JavaSet<>(copy);
    }

    public Stream<T> stream() {
        return Streams.from(this.set);
    }
}
