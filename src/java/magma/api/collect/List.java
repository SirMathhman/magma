package magma.api.collect;

import magma.api.stream.Stream;

public interface List<T> {
    List<T> add(T other);

    Stream<T> stream();
}
