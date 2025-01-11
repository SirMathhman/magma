package magma.java;

import magma.stream.HeadedStream;
import magma.stream.Stream;

import java.util.ArrayList;
import java.util.Set;

public record JavaSet<T>(Set<T> sources) {
    public Stream<T> stream() {
        return new HeadedStream<>(new JavaListHead<>(new ArrayList<>(sources)));
    }
}