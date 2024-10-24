package magma.api.list;

import magma.api.option.Option;
import magma.api.stream.Stream;

public interface List<T> {
    Stream<T> stream();

    int size();

    Option<T> get(int index);
}
