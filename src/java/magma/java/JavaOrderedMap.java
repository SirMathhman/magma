package magma.java;

import magma.api.Tuple;
import magma.api.option.Option;
import magma.api.stream.Stream;

public record JavaOrderedMap<K, V>(JavaList<Tuple<K, V>> tuples) {
    public JavaOrderedMap() {
        this(new JavaList<>());
    }

    public JavaOrderedMap<K, V> put(K key, V value) {
        return new JavaOrderedMap<>(tuples.addLast(new Tuple<>(key, value)));
    }

    public Option<Integer> findIndexOfKey(K key) {
        return tuples.streamWithIndex()
                .filter(tuple -> tuple.right().left().equals(key))
                .next()
                .map(Tuple::left);
    }

    public Option<JavaOrderedMap<K, V>> sliceToIndex(int index) {
        return tuples.sliceToIndex(index).map(JavaOrderedMap::new);
    }

    public Stream<Tuple<K, V>> stream() {
        return tuples.stream();
    }

    public Option<V> find(K propertyKey) {
        return tuples.stream()
                .filter(tuple -> tuple.left().equals(propertyKey))
                .map(Tuple::right)
                .next();
    }
}
