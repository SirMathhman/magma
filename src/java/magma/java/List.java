package magma.java;

import magma.api.stream.Stream;

public interface List<T> {
    Stream<T> stream();
}
