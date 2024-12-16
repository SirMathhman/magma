package magma.api.collect;

import magma.api.stream.Stream;
import magma.api.stream.Streams;

import java.util.ArrayList;

public record MutableList<T>(java.util.List<T> list) implements List<T> {
    public MutableList() {
        this(new ArrayList<>());
    }

    @Override
    public List<T> add(T other) {
        list.add(other);
        return this;
    }

    @Override
    public Stream<T> stream() {
        return Streams.from(list);
    }
}
