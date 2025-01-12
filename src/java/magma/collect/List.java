package magma.collect;

import magma.java.JavaList;
import magma.option.Option;
import magma.stream.Stream;

public interface List<T> {
    JavaList<T> add(T next);

    int size();

    Option<List<T>> slice(int start, int end);

    Stream<T> stream();
}
