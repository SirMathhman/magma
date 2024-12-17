package magma.api.collect;

import magma.api.Tuple;
import magma.api.stream.Stream;

public interface List<T> {
    List<T> add(T other);

    Stream<T> stream();

    Stream<Tuple<Integer, T>> streamWithIndices();
}
