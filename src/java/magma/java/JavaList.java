package magma.java;

import magma.api.stream.Stream;
import magma.api.stream.head.HeadedStream;
import magma.api.stream.head.ListHead;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        return new HeadedStream<>(new ListHead<>(list));
    }
}
