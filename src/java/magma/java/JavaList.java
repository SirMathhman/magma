package magma.java;

import magma.stream.HeadedStream;
import magma.stream.ListHead;
import magma.stream.Stream;

import java.util.ArrayList;
import java.util.List;

public record JavaList<T>(List<T> list) {
    public JavaList() {
        this(new ArrayList<>());
    }

    public JavaList<T> add(T node) {
        list.add(node);
        return this;
    }

    public Stream<T> stream() {
        return new HeadedStream<>(new ListHead<>(list));
    }
}
