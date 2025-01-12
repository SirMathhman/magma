package magma.collect;

import magma.stream.Stream;

public interface Set<T> {
    default Set<T> add(T next) {
        this.internal.add(next);
        return this;
    }

    Stream<T> stream();
}
