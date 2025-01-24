package magma.java;

import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.api.stream.Stream;
import magma.api.stream.Streams;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public record JavaList<T>(List<T> list) {
    public JavaList() {
        this(Collections.emptyList());
    }

    @SafeVarargs
    public static <T> JavaList<T> of(T... values) {
        return new JavaList<>(Arrays.asList(values));
    }

    public Option<JavaList<T>> subList(int from, int to) {
        if (from >= 0 && from <= this.list.size() && to >= 0 && to <= this.list.size() && from <= to) {
            return new Some<>(new JavaList<>(this.list.subList(from, to)));
        }
        return new None<>();
    }

    public JavaList<T> add(T element) {
        final var copy = new ArrayList<>(this.list);
        copy.add(element);
        return new JavaList<>(copy);
    }

    public Stream<T> stream() {
        return Streams.fromNativeList(this.list);
    }

    @Deprecated
    public List<T> unwrap() {
        return this.list;
    }
}
