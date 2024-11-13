package magma.java;

import magma.api.Tuple;
import magma.api.option.Option;
import magma.api.stream.HeadedStream;
import magma.api.stream.Stream;

public record JavaOrderedMap<K, V>(JavaList<Tuple<K, V>> tuples) {
    public JavaOrderedMap() {
        this(new JavaList<>());
    }

    public JavaOrderedMap<K, V> put(K key, V value) {
        return new JavaOrderedMap<>(tuples.add(new Tuple<>(key, value)));
    }

    public Option<Integer> findIndexOfKey(K key) {
        return tuples.streamWithIndex()
                .filter(tuple -> tuple.right().left().equals(key))
                .next()
                .map(Tuple::left);
    }
}
