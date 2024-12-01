package magma.java;

import magma.api.Tuple;
import magma.api.option.Option;
import magma.api.stream.Stream;

public record JavaOrderedMap<K, V>(JavaList<Tuple<K, V>> list) {
    public JavaOrderedMap() {
        this(new JavaList<>());
    }

    Option<Integer> findKeyIndex(String key) {
        return streamWithIndices()
                .filter(tuple -> tuple.right().left().equals(key))
                .map(Tuple::left)
                .next();
    }

    public JavaOrderedMap<K, V> put(K key, V value) {
        final var head = list.stream()
                .filter(entry -> entry.left().equals(key))
                .next();

        if (head.isEmpty()) {
            return new JavaOrderedMap<>(list.add(new Tuple<>(key, value)));
        }

        return this;
    }

    public Stream<Tuple<K, V>> stream() {
        return list.stream();
    }

    public Stream<Tuple<Integer, Tuple<K, V>>> streamWithIndices() {
        return list.streamWithIndices();
    }

    public Option<Stream<Tuple<K, V>>> sliceTo(int index) {
        return list.sliceTo(index);
    }

    public Option<Tuple<K, V>> get(int index) {
        return list.get(index);
    }

    public Option<V> getValue(int index) {
        return get(index).map(Tuple::right);
    }

    @Override
    public String toString() {
        return "{" + list.stream()
                .map(pair -> pair.left() + "=" + pair.right())
                .foldLeft((previous, next) -> previous + ", " + next).orElse("") + "}";
    }

    public Option<V> find(K key) {
        return list.stream()
                .filter(element -> element.left().equals(key))
                .map(Tuple::right)
                .next();
    }
}
