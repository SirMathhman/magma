package magma;

import magma.option.Option;

public interface Map<K, V> {
    Map<K, V> put(K key, V value);

    Stream<Tuple<K, V>> stream();

    Option<V> find(K key);
}
